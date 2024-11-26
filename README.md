# Getting Started
This application translates IT jargon to plain language. The service uses Google Cloud Speech-to-Text and Text-to-Speech APIs to convert audio to text and text to audio. 
The service also uses the ChatGPT API to generate responses to user queries.
In order to use this application, you need to set up Google Cloud and OpenAI accounts and obtain the required tokens.
For more information on setting up the required tokens and environment variables, refer to the Setup Google Cloud Account
and Setup OpenAI Account sections.

### Setup Google Cloud Account
1. Go to https://console.cloud.google.com.
2. Create new account/sign in to your Google Cloud account.
3. Create a new project.
4. Open it and search for "Cloud Speech-to-Text API".
5. Press "Enable" button.
6. Search for "Cloud Text-to-Speech API" and press "Enable" button.
7. Open menu in the top left corner and select IAM & Admin -> Service Accounts.
8. Press Create Service Account.
9. Type in the name of the service account and press Create And Continue.
10. Select the role Project -> Cloud Speech Administrator.
11. Press "Add another role" and choose "Editor" and press Continue. 
12. Press Done. 
13. You will see the service account you created. Press the three dots on the right and select Manage keys. 
14. Press Add Key -> Create new key. 
15. Select JSON and press Create. 
16. Set the environment variable GOOGLE_APPLICATION_CREDENTIALS to the path of the JSON file you downloaded.

### Setup OpenAI Account
1. Go to https://platform.openai.com.
2. Search for API keys in the search bar.
3. Press Create new secret key.
4. Type in the name of the key and press Create secret key.
5. Press Copy to copy the key to your clipboard.
6. Set openai.api.key environment variable to the key you copied.

### Running the Application
1. Run the application using the following command:
```sh  
mvn spring-boot:run
```
2. Open index.html file that is located in the src/main/resources/static directory in your browser.
3. Press the Start button to start recording audio.
4. Speak into the microphone. The application will translate your IT jargon into plain human language.
5. Press the Stop button to stop recording audio.
