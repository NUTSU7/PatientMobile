package com.semanticsoft.patientmobile.feature.laborator.dashboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LaboratorDashboardViewModelTest {

    @Test
    fun initialState_usesThirtyDayDesignData() {
        val viewModel = LaboratorDashboardViewModel()
        val state = viewModel.state.value

        assertEquals(LaboratorPeriod.THIRTY_DAYS, state.selectedPeriod)
        assertEquals(4, state.metrics.size)
        assertEquals("1.240", state.metrics.first { it.id == "processed" }.value)
        assertEquals("8.302", state.metrics.first { it.id == "accessed" }.value)
        assertEquals(5, state.topAnalyses.size)
        assertTrue(state.lastSyncLabel.startsWith("Ultima sincronizare: "))
    }

    @Test
    fun selectingSevenDays_updatesMetricsAndPeriod() {
        val viewModel = LaboratorDashboardViewModel()

        viewModel.onPeriodSelected(LaboratorPeriod.SEVEN_DAYS)
        val state = viewModel.state.value

        assertEquals(LaboratorPeriod.SEVEN_DAYS, state.selectedPeriod)
        assertEquals("312", state.metrics.first { it.id == "processed" }.value)
        assertEquals("2.104", state.metrics.first { it.id == "accessed" }.value)
        assertEquals(7, state.activity.processed.size)
    }

    @Test
    fun compareToggle_updatesComparisonState() {
        val viewModel = LaboratorDashboardViewModel()

        viewModel.onCompareToggle(false)

        assertFalse(viewModel.state.value.compareEnabled)
    }

    @Test
    fun refresh_updatesLastSyncLabel() {
        val viewModel = LaboratorDashboardViewModel()

        viewModel.refresh()

        assertTrue(viewModel.state.value.lastSyncLabel.startsWith("Ultima sincronizare: "))
        assertFalse(viewModel.state.value.lastSyncLabel.endsWith("—"))
    }
}
