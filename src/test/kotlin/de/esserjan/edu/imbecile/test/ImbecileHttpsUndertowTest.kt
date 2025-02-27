package de.esserjan.edu.imbecile.test

import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit

@Timeout(value = 10L, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class ImbecileHttpsUndertowTest : ImbecileHttpsTest(GitUndertowMock)