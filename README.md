<p align="center"><img width=13.5% src="https://github.com/yashprakash13/Remember--Store-words-pronunciation-vocab-builder/blob/master/readme/images/512.png"></p>

<h1 align="center">Remember</h1>

<p align="center">Store new words that you learn along with their pronunciation! Build your vocab!</p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<p align="center"><img src="https://img.shields.io/badge/Made%20Using-Kotlin-brightgreen?style=for-the-badge"></p>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

### Get the app on [Google Play](https://play.google.com/store/apps/details?id=save.newwords.vocab.remember).

## Table of contents
- [Basic Usage of the app](#basic-usage)
- [The Architecture and Components used in the Project](#components-and-architecture)
- [Screenshots of the app in action](#a-few-screenshots)
- [Contributing](#contribute-to-this-repo)

## Basic Usage 

You can:

1. Add new words that you learn
2. Add word meanings
3. Add your own pronunciation for the words you store

> This is a simple app designed to help you manage your vocabulary and track your progress in learning new words.

## Components and Architecture
**Remember** follows the MVVM (Model View View-Model) app architectural pattern with strictly defined Presentation, Business logic and Model directories for clear separation of concerns. The following are the major components of this app:
* MVVM architecture with :
  - Room database model, dao object class and database instantiation class defined in the `db` directory
  - `repository` package for defining the database access methods which will be accessed in our fragments
  - `ui` folder for the views and viewmodels of our screens
* `common` package contains some independent classes, extension methods and constants for use in the fragments and viewmodel classes
*  This app follows a single activity approach with multiple fragments for different tasks defined as:
  - HomeFragment for displaying list of words
  - NewWordFragment for entering new word in db
  - EditWordFragment for editing an existing word
  - OptionsFragment for the user preferences in the app
* User preferences (settings) fragment for modifying the following settings in the app:
  - whether to tap once or twice to edit a word in the app
  - whether to show meanings by default in lists (grid and list)
  - set a daily reminder (Implemeted via Work Mananger)
  - other options such as privacy policy.
* Navigation component for navigating between fragments and safeargs for object passing
* Options to sort words by recently entered or albhabetically (via BottomSheet )
* Material design components such as:
  - BottomAppBar 
  - Bottomsheet
  - FloatingActionButton
  - Material Textfields and Inputfields with Buttons (Outlined, Text and Normal)
* Storing audio pronunciations in internal storage


 
## A Few Screenshots

<img width=43.5% src="https://github.com/yashprakash13/Remember--Store-words-pronunciation-vocab-builder/blob/master/readme/images/screenshots/Phone%20Screenshot%201.jpg">  <img width=43.5% src="https://github.com/yashprakash13/Remember--Store-words-pronunciation-vocab-builder/blob/master/readme/images/screenshots/Phone%20Screenshot%202.jpg">
<img width=43.5% src="https://github.com/yashprakash13/Remember--Store-words-pronunciation-vocab-builder/blob/master/readme/images/screenshots/Phone%20Screenshot%203.jpg">  <img width=43.5% src="https://github.com/yashprakash13/Remember--Store-words-pronunciation-vocab-builder/blob/master/readme/images/screenshots/Phone%20Screenshot%204.jpg">
<img width=43.5% src="https://github.com/yashprakash13/Remember--Store-words-pronunciation-vocab-builder/blob/master/readme/images/screenshots/Phone%20Screenshot%205.jpg">

## Contribute To This Repo
If you have an idea to improve this app or want to make a contribution, please do open an issue to get in touch!

## Support me
I'll appreciate it!

[![ko-fi](https://www.ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/G2G3R125)
