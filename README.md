# Darcs Plugin for Jenkins CI

This plugin integrates the [Darcs][1] SCM into [Jenkins CI][2]. The official plugin site
is [here][3].

## Installation

You can install the plugin via the Jenkins plugin management site. This plugin requires a
Darcs installation on the machine where it is installed.

## Developers

As prerequisite you need [Apache Maven][4] installed.

In your `~/.m2/settings.xml` you need the Jenkins tools plugin group:

```XML
<pluginGroups>
    ...
    <pluginGroup>org.jenkins-ci.tools</pluginGroup>
    ...
</pluginGroups>
```

And for some backword compatibility a mirror:

```XML
<mirrors>
    ...
    <mirror>
        <id>repo.jenkins-ci.org</id>
        <url>http://repo.jenkins-ci.org/public/</url>
        <mirrorOf>m.g.o-public</mirrorOf>
    </mirror>
    ...
</mirrors>
```

Clone this repo with:

    $ git clone git://github.com/Weltraumschaf/jenkins-darcs.git

To compile the source change into the repo and type:

    $ mvn install

For running the plugin from the repo locally type:

    $ mvn hpi:run

Information about how to write a plugin for Jenkins can be found in this [plugin tutorial][5].
Informations about the general architecture of Jenkins can be found [here][6]. And last but not
least [here][7] are some informations about how to host a Jenkins plugin.

## Todo

- write tests for DarcsScm
- write tests for BrowserChooser
- write tests for repo browser
- improve the change set index view
- translate help texts
- reorganize stuff from webapp dir (help) into resources dir

[1]: http://darcs.net/
[2]: http://www.jenkins-ci.org/
[3]: http://wiki.jenkins-ci.org/display/JENKINS/Darcs+Plugin
[4]: http://maven.apache.org/
[5]: https://wiki.jenkins-ci.org/display/JENKINS/Plugin+tutorial
[6]: https://wiki.jenkins-ci.org/display/JENKINS/Architecture
[7]: https://wiki.jenkins-ci.org/display/JENKINS/Hosting+Plugins
