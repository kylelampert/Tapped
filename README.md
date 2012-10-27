Tapped
======

Demo applications for the Tapped NFC Hackathons. See http://www.tappednfc.com for more information.

Contents:
- Tapped Developer Guide: A bare-bones application that demos basic NFC functionality. This codebase accompanies the Tapped Developer guide (http://www.tappednfc.com/android-nfc-developer-guide/).
- Tapped Demo: A demo application featuring Android Beam, tag reading & writing, and Facebook integration. Also demos use of Kinvey as a cloud-based backend and datastore.

Installation Notes:
- See the Tapped Developer Guide (in the Wiki of this repo) for getting started with Eclipse and the Android SDK.
- Ensure your Java compiler version for each project is set at 1.6. It may default to 1.5.
-  |_> Right click on the project in Eclipse, select Properties, and you'll find this setting under "Java Compiler"
- The demo applications should compile and run without any additional effort. Facebook and Kinvey will not be functional until you create applications with each service.
- Kinvey: Visit https://console.kinvey.com/#signup to create an account and generate application keys for your application
- Facebook: See 'Create an App' under https://developers.facebook.com/docs/getting-started/facebook-sdk-for-android/3.0/
