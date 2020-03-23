# Swiss-Knife

This is a Kotlin implementation of the [Swiss-Knife distance bounding protocol](https://perso.uclouvain.be/fstandae/PUBLIS/60.pdf).

# How to use

## Setup

Use the latest distribution, or build the library with `./gradlew build`.

### In a Gradle project

Put `swiss-knife.jar` in a `libs` folder and add the following line in your `build.gradle`:

```groovy
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
}
```

## Code

You need to create two classes extending the `fr.yopox.swiss_knife.Tag` and `fr.yopox.swiss_knife.Reader` abstract classes.

You can change :

- `Values.m` : Number of rounds
- `Values.T` : Faults tolerance
- `Values.speed` : Wave speed for the quick phase (phase 2)
- `Values.C_B` : System constant

Then call `MyReader#start` and `MyTag#start`. `MyReader#start` returns `true` if the tag is verified.

# Test

A test is included in `main/test/kotlin/fr/yopox/swiss_knife/`. `LHTag` and `LHReader` communicate via a socket during the three phases of the protocol.

You can test if it is working by executing `gradle test`.