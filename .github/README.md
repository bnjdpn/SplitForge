# Local verification

Studio config/ci.json runs `.github/check-local` at the PR SHA through
`bin/studio-ci bnjdpn/SplitForge <sha>`. The Mac needs JDK 17, the Android SDK
and Xcode. The script preserves Gradle tests, Android debug build and lint,
and both iOS simulator framework builds from the removed workflow.
The duplicate Gradle test invocation in the old iOS job is run once.

No native check was executed during Studio #469 corrections because the
simulator slot is reserved by #431. Tech must obtain studio/ci success before merge.
