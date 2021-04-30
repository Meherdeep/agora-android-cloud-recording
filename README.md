# Agora Cloud Recording Demo - Android

A quickstart guide on how to use the Agora Cloud Recording SDK to record all your video calls through your Android Application.

## Prerequisites

- An Agora developer account (see [How to get started](https://www.agora.io/en/blog/how-to-get-started-with-agora))
- An AWS account
- Heroku account to deploy your backend
- VS Code
- A basic understanding of Flutter Development

## Create an Agora account 

To build and run this sample app, first create an Agora developer account over [here](http://console.agora.io/)
- After creating a project head on to the [Project Management](https://console.agora.io/projects) tab to create a new project.
- Choose secured mode for authentication and click on submit 
- Next to your project and under the Action column click on the Edit button 
- From this page copy your App Id, App Certificate and then scroll at the bottom and enable the cloud recording functionality.
- After this head on to the [RESTful API section](https://console.agora.io/restfulApi) 
- On this page click on `Add a secret` button to generate your Customer ID and Customer Secret. Save the value hence generated as we will be needing them when we deploy our server. 

## Setup your AWS S3 Bucket

Create an AWS account if you don't have it already. 
- Head on to your AWS IAM Console, create a new user, and make sure that you add AmazonS3FullAccess policy with Programmatic Access.
- Once done copy your AWS Access Key and Secret Key to a text file.
- Create a new AWS S3 Bucket. Give it an appropriate name and copy the name to your clipboard so that we can use it later. If you already have a bucket you can skip this step.
- You will need to find out your region number (depending on your AWS server region), visit this table and click on Amazon S3 tab to see the list.

## Deploying your backend

Before deploying ensure that you have acquired the following :
```
APP_ID=
APP_CERTIFICATE=
RECORDING_VENDOR=
RECORDING_REGION=
BUCKET_NAME=
BUCKET_ACCESS_KEY=
BUCKET_ACCESS_SECRET=
CUSTOMER_ID=
CUSTOMER_CERTIFICATE=
```

You can simply use this Heroku one click deploy to deploy your own backend to Heroku's server: 

[![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy?template=https://github.com/raysandeep/Agora-Cloud-Recording-Example/)

You can also look at the Postman documentation given over here to know more about the method calls

[![Run in Postman](https://run.pstmn.io/button.svg)](https://documenter.getpostman.com/view/8653133/TzCS4RCq)

## Setting it all up

Once you've deployed your own backend head on to the `Constants.kt` file and add your app id and channel name.

```
val appId = "my-app-id";

val channelName = "test" //Replace this with a channel name of your choice
```

And then head on to `MainActivity.kt` enter the baseUrl of your token server: 

```
val baseUrl: String = "" // Enter the link to your token server
```

## Resources

- You can find the complete API Documentation over [here](https://docs.agora.io/en/Video/API%20Reference/flutter/index.html).
- I also invite you to join the [Agora Developer Slack Community](https://agoraiodev.slack.com/).