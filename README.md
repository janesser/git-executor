# git-executor

 git-executor provides an osgi bundle for the [git --distributed-is-the-new-centralized}(https://git-scm.com/).

## Discussion started over here

<https://github.com/eclipse-egit/egit/issues/47>

The driving thought for others and also git-executor is the observation that git maintainers spend their main effort on git-cli.

Secondary fundamental is to let git-executor be driven by what ever pleases in the osgi world, and drive git-cli accordingly (hex-arch).

This comes with batteries included, which will be _more or less_ seen by the security considerations when dealing with identities.

## Great Resources that have helped doing this

<https://vogella.com/blog/getting-started-with-osgi-declarative-services-2024/>
(this helped to figure out how to auto-generate the declarative xml for osgi-components)

to be continued...

## Next steps

1. Get reasonable use `git credentials` to work

1. <https://www.vogella.com/tutorials/EclipseTycho/article.html>

to be continued...
