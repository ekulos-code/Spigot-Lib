# Spigot Plugin Library
![GitHub Workflow Status](https://img.shields.io/github/workflow/status/PierreSchwang/Spigot-Lib/Deploy%20API?style=flat-square)
![GitHub pull requests](https://img.shields.io/github/issues-pr/PierreSchwang/Spigot-Lib)
![GitHub stars](https://img.shields.io/github/stars/PierreSchwang/Spigot-Lib?style=flat-square)
![GitHub forks](https://img.shields.io/github/forks/PierreSchwang/Spigot-Lib?style=flat-square)
## Maven 
````xml
<repositories>
    <repository>
        <id>pierre-nexus</id>
        <url>https://repo.pschwang.eu/repository/maven-snapshots/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>de.pierreschwang</groupId>
        <artifactId>spigot-lib</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
</dependencies>
````

## Getting started
### Create the base plugin

````java
public class ExamplePlugin extends AbstractJavaPlugin<ExampleUser> {

    @Override
    public void onAbstractEnable() {
        // Things to call when the plugin gets enabled
    } 
    
    @Override
    public UserFactory<ExampleUser> getUserFactory() {
        return player -> new ExampleUser(this, player); // Set the user factory
    }
    
    // Test method to show some utilities
    public void sendMessageToUser(Player player) {
        getUser(player).sendMessage("your-locale-key", param1, param2); // Sends a localized message to the user
    } 

}
````

````java
/**
  * You have to create your own implementation of the user for your plugin. 
  * Inside this object you can store custom data which may be needed and also have access to methods from the base user.
  */
@Getter
public class ExampleUser extends User {

    private int customUserData;
    private String evenMoreCustomData;

    public BedwarsPlayer(ExamplePlugin plugin, Player player) {
        super(plugin, player);
    }

}
````

## Localization
### General usage
This api supports also the use of localized messages - That means, the player is able to retrieve message in the language of his client.
To start, simple create a folder called `lang` under src/main/resources. Inside this folder create your language files, e.g. `en_US.properties` or `de_DE.properties`.
<br />
An example language file might look like this:
````properties
prefix = MyCoolPlugin >
test-message = %prefix% &6Hello {0}, the current time is: {1}
````
The ``%prefix%`` parameter is the only placeholder that gets replaced with the defined ``prefix`` locale string. The `{0}`,`{1}`,`{n}`, ... defines parameters, which are passes in the `sendMessage()` method.

### Using localization when a player joins
Because the ``PlayerJoinEvent`` gets called before the server recieves the language information from the client, localization does not work with this particular event. If you need localization when a player joins the server (e.g. for a join message or some hotbar items), you can use the ``PlayerReadyEvent<ExampleUser>``, which gets fired slightly after the ``PlayerJoinEvent``, as soon as the language has been initialized. 
````java
@EventHandler
public void onPlayerReady(PlayerReadyEvent<ExampleUser> event) {
    event.getUser().sendMessage("your-welcome-message");
}
````

## ToDo
 + More Documentation incl Hosted Javadocs
 + Querybuilder (simple database operations)
 + Achievements
 + ConnectionProvider
 + NPC Library
 + Stats
 + Title, Subtitle, Actionbar
