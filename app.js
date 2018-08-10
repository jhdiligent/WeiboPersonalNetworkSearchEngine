var express = require("express");
var app = express();
var bodyParser = require("body-parser");

var elasticsearch = require('elasticsearch');
var client = new elasticsearch.Client({
    host: 'localhost:9200',
    log: 'trace'
});

app.set("view engine","ejs");
app.use(bodyParser.urlencoded({extended:true}));

//functions
async function searchUser(ms,size,theQuery){
    var response=await client.search({
        index: 'weibo',
        type: 'userinfo',
        body: {
            "min_score":ms,
            "size":size,
            "query": {
                "function_score": {
                    "query": {
                        "match": {
                            NickName: theQuery
                        }
                    },
                    "script_score": {
                        "script": "0.001*Math.log(doc['Num_Fans'].value+1)/(doc['Num_Follows'].value+1000)+_score"
                    }
                }
            },
            "highlight": {
                "pre_tags": ["<b style='background-color: yellowgreen'>"],
                "post_tags": ["</b>"],
                "fields": {
                    "NickName": {}
                }
            }
        }
    });
    return response.hits.hits;
}

async function searchUserbyID(ID){
    var response = await client.search({
        index: 'weibo',
        type: 'userinfo',
        body: {
            "size":1,
            "query": {
                "function_score": {
                    "query": {
                        "match": {
                            "URL": ID
                        }
                    }
                }
            }
        }
    });
    return response.hits.hits;
}

async function searchTweet(ms,size,theQuery){
    var response=await client.search({
        index: 'sina',
        type: 'tweets',
        body: {
            "min_score": ms,
            "size":size,
            "query": {
                "function_score": {
                    "query": {
                        "match": {
                            "Content": theQuery
                        }
                    },
                    "script_score": {
                        "script": "0.01*Math.log(30*doc['Transfer'].value+50*doc['Comment'].value+20*doc['Like'].value+1)+_score"
                    }
                }
            },
            "highlight": {
                "pre_tags": ["<b style='background-color: yellowgreen'>"],
                "post_tags": ["</b>"],
                "fields": {
                    "Content": {}
                }
            },
            "sort": [
                {"_score": {"order": "desc"}},
                {"epoch": {"order": "desc"}}
            ]
        }
    });
    return response.hits.hits;
}


//REST
app.get("/",function(req,res){
   res.render("index");
});

var query=null;
app.post("/results-tweet",function (req,res) {
    query = req.body.query;
    res.redirect("/results-tweet");
})
app.get("/results-tweet",async function(req,res) {
    //search tweets
    var tweets=await searchTweet(6.5,50,query);
    res.render("results-tweet",{tweets:tweets, curQuery:query});
});
app.post("/results-user",function (req,res) {
   query=req.body.query;
   res.redirect("/results-user");
});
app.get("/results-user",async function (req,res) {
    var users = await searchUser(6.5,50,query);
    //add SupElement
    var tweets=await searchTweet(6.5,50,query);
    var supUsers=[];
    var supTweets=[];
    for(var i=0;i<tweets.length;i++){
        var tweet = tweets[i];
        var ID = tweet._source.ID;
        var usera = await searchUserbyID(ID);
        var user = usera[0];
        if(user){
            console.log(i);
            console.log(user);
            supUsers.push(user);
            supTweets.push(tweet);
        }
    }
    res.render("results-user",{users:users,curQuery:query,supUsers:supUsers,supTweets:supTweets});
});

app.listen(3000);