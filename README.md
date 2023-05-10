# Needle
*Minecraft Version 1.19.4*

<p>
<a href="https://opensource.org/licenses/MIT"><img src="https://img.shields.io/badge/License-MIT-brightgreen.svg"></a>
<a href="https://maven.jtrim777.dev/#/releases/dev/jtrim777/needle/0.1.4"><img src="https://maven.jtrim777.dev/api/badge/latest/releases/dev/jtrim777/needle?color=7011ff&name=Maven&prefix=v"></a>
</p>


`needle` is a library to ease the development of Minecraft Mods in
the Fabric environment. It takes some of its core ideas from my other
(related) project [scalacore](https://github.com/jtrim777-dev/mc-scalacore),
but with some newer syntactic sugar, a little less clutter, and no Forge.

## Usage

To use in modding you'll need to add my Maven repository to your repo list
in `build.gradle`:

```groovy
repositories {

    maven {
        name = "jtrim777"
        url = "https://maven.jtrim777.dev/releases"
    }
}

dependencies {
    modImplementation "dev.jtrim777:needle:${project.needle_version}"
}
```

In `gradle.properties` you can set `needle_version` to the latest
version of this library (see the badge at the top of this README).