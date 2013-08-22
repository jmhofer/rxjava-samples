resolvers += "Local Maven Repository" at Path.userHome.toURI + ".m2/repository"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.1")