To use this example
1. Need to create account in https://parse.com
2. Create a Application
3. Get application and client key under settings tab
4. Copy application and client key in opendevice android code/app/src/main/java/com/shashi/opendevice/StarterApplication.java - where it is menstioned in string
application and client id
5. Build the android code using Android SDK
6. Deploy .apk file to mobile or emulator device

7. Again copy application and rest api key under settings to opendevice/src/main/java/com/shashi/BulbDemo.java  -  where it is menstioned in string
application id and rest api id

Step to deploy Parse Cloud code.(It is used because parse java api doesnt support push notification so from java we call cloud code method from there we will 
push to mobile devices)

1. Go inside parse cloud code folder and open command promt
2. Type parse new and it askes for email and password of parse account
3. Now it askes for existing application or new application select 'e' and enter
4. Select your application from listed populated by command promt
5. And then Folder will be create of your application name
6. inside that folder there is folder called cloud and there open main.js 
7. There you copy paste code from cloud/main.js

Now your set to go.

Run java application by connecting arduino board and upload code provided.
Then When the Bulb event is changed then the notification is received in mobile. Enjoy:)
