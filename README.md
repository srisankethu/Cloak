# Cloak

Currently it is a very basic program which allows you to select an object or region which will tracked and cloaked on double-click. It has two cloaking effects at present, namely `vanish` and `pixelblur`.

## Setup

1. `git clone https://github.com/srisankethu/Cloak.git`
2. `cd Cloak/cloak`
3. `mvn package`
4. `java -cp target/cloak-1.0-SNAPSHOT.jar com.github.cloak.App`

## Instructions

1. Click and drag the target rectangle. Leave mouse when object/region is selected.
2. Double-click to disable.
3. Change effects by commenting the other effect in the [code](https://github.com/srisankethu/Cloak/blob/master/cloak/src/main/java/com/github/cloak/App.java#L100)(for now).

## TODO

* Add error handling.
* Fix existing bugs like enabling cloaking on click.
* Add tests.
* Add more cloaking effects.
* Add and combine more tracker types support to enhance object tracking.

**Note:** All relevant documentation is written in the code documentation which is *Javadoc complaint*.
