# imbécile

imbécile provides an osgi bundle that wraps around the CLI of [git --distributed-is-the-new-centralized](https://git-scm.com/).

## why "imbécile" ?

    "git" can mean anything, depending on your mood.

    - Random three-letter combination that is pronounceable, and not actually used by any common UNIX command. The fact that it is a mispronunciation of "get" may or may not be relevant.
    - Stupid. Contemptible and despicable. Simple. Take your pick from the dictionary of slang.
    - "Global information tracker": you're in a good mood, and it actually works for you. Angels sing, and a light suddenly fills the room.
    - "Goddamn idiotic truckload of sh*t": when it breaks.

Excerpt from [Wikipedia (Git)](https://en.wikipedia.org/wiki/Git).

Sticking to the original theme of "git" and being fan of linux kernel newsletter since the nineties,
[imbécile](https://cnrtl.fr/definition/imb%C3%A9cile) simply nails it.

### Discussion started over here

<https://github.com/eclipse-egit/egit/issues/47>

The driving thought for others and also git-executor is the observation that git maintainers spend their main effort on git-cli.

Secondary fundamental is to let git-executor be driven by what ever pleases in the osgi world, and drive git-cli accordingly (hex-arch).

This comes with batteries included, which will be _more or less_ seen by the security considerations when dealing with identities.

### Great Resources that have helped doing this

<https://vogella.com/blog/getting-started-with-osgi-declarative-services-2024/>
(this helped to figure out how to auto-generate the declarative xml for osgi-components)

to be continued...

## Next steps

1. Get reasonable use `git credentials` to work
2. Publish on maven central

to be continued...
