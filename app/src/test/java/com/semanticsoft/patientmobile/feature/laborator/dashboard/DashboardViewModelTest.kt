package com.semanticsoft.patientmobile.feature.laborator.dashboard

import com.semanticsoft.patientmobile.feature.laborator.common.Period
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardViewModelTest {

    @Test
    fun initialState_usesThirtyDayDesignData() {
        val viewModel = DashboardViewModel()
        val state = viewModel.state.value

        assertEquals(Period.THIRTY_DAYS, state.selectedPeriod)
        assertEquals(4, state.metrics.size)
        assertEquals("1.240", state.metrics.first { it.id == "processed" }.value)
        assertEquals("8.302", state.metrics.first { it.id == "accessed" }.value)
        assertEquals(5, state.topAnalyses.size)
        assertTrue(state.lastSyncLabel.startsWith("Ultima sincronizare: "))
    }

    @Test
    fun selectingSevenDays_updatesMetricsAndPeriod() {
        val viewModel = DashboardViewModel()

        viewModel.onPeriodSelected(Period.SEVEN_DAYS)
        val state = viewModel.state.value

        assertEquals(Period.SEVEN_DAYS, state.selectedPeriod)
        assertEquals("312", state.metrics.first { it.id == "processed" }.value)
        assertEquals("2.104", state.metrics.first { it.id == "accessed" }.value)
        assertEquals(7, state.activity.processed.size)
    }

    @Test
    fun compareToggle_updatesComparisonState() {
        val viewModel = DashboardViewModel()

        viewModel.onCompareToggle(false)

        assertFalse(viewModel.state.value.compareEnabled)
    }

    @Test
    fun refresh_updatesLastSyncLabel() {
        val viewModel = DashboardViewModel()

        viewModel.refresh()

        assertTrue(viewModel.state.value.lastSyncLabel.startsWith("Ultima sincronizare: "))
        assertFalse(viewModel.state.value.lastSyncLabel.endsWith("—"))
    }
}
