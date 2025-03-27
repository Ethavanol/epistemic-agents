# Epistemic Agents 
[![CircleCI](https://circleci.com/gh/MikeVezina/epistemic-agents/tree/master.svg?style=svg&circle-token=d7ce6dbdee725382aab008ae3406668de1e409d7)](https://circleci.com/gh/MikeVezina/epistemic-agents/tree/master)

- Tests are currently out-of-date but implementation is working (prioritizing implementation completion for deadline)

- **Requirement**: The epistemic reasoner used by these agents can be found at: https://github.com/MikeVezina/epistemic-reasoner
- **Example**: A demo of how this framework can be used is shown for agent localization at: https://github.com/MikeVezina/localization-demo


## Install Gradle Local Dependency
First of all, make sure to rename the "reasoner-config.json.example" in "reasoner-config.json".

In order to use the epistemic agent framework with other projects, you must install it into your local gradle repository (it is currently not being published to maven central). 

To do this, make sure gradle is installed, clone the repository, and run: 
   
     ./gradlew publishToMavenLocal 
   
If some tests don't pass, run :  

    ./gradlew publishToMavenLocal -x test 


To check that it has been installed correctly, you can verify that the folowing folder exists in your local maven repo:

    com\mvezina\epistemic-agents

The maven repo should be in the following folder depending on your OS:

Windows:

    C:\Users\<User_Name>\.m2

Linux:

    /home/<User_Name>/.m2

Mac:

    /Users/<user_name>/.m2

And of course, for Linux and Mac, we can write in the short form:

    ~/.m2

Everything should be good now
