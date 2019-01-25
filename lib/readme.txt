These files are added here temporarily to keep the project compiling whilst the underlying issues are resolved.

bcprov-jdk15-on-161b20.jar: There are some classes (e.g.ECParameterSpec) that are only provided in 161-beta; we will revert this once this has been published
kotlin-multibase-1.1.0.jar: This doesn't appear to be available in any maven public repo, but is a compile-time dependency for kotlin-multiaddr