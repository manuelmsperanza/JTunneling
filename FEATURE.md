# Feature Notes

## Java 25 readiness
- Build toolchain is configured for Java 25 (`source`, `target`, and compiler `release`).
- Maven enforcer now validates Java 25+ and Maven 3.9+.

## Build stability
- Artifact signing has been moved behind an explicit `sign-artifacts` Maven profile so regular `mvn install` no longer requires keystore secrets.

## Quality and verification
- Javadoc generation is explicitly configured and attached during builds.
- Added a JUnit 5 unit test to verify `TunnelingMonitor.closeAll()` disconnects every tracked tunnel.
