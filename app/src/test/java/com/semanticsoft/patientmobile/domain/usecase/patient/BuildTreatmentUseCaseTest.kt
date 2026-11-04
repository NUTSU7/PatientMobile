package com.semanticsoft.patientmobile.domain.usecase.patient

import com.semanticsoft.patientmobile.domain.model.patient.DoseUnit
import com.semanticsoft.patientmobile.domain.model.patient.MealRelation
import com.semanticsoft.patientmobile.domain.model.patient.MedicationDraft
import com.semanticsoft.patientmobile.domain.model.patient.MedicationSchedule
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildTreatmentUseCaseTest {

    private val useCase = BuildTreatmentUseCase()
    private val start = LocalDate.of(2026, 1, 10)

    private fun draft(
        name: String = "Augmentin",
        doseText: String = "",
        durationText: String = "",
        schedules: List<MedicationSchedule> = emptyList(),
        requiresOcrReview: Boolean = false,
        reviewed: Boolean = false
    ) = MedicationDraft(
        draftId = "d-$name",
        name = name,
        doseText = doseText,
        durationText = durationText,
        startDate = start,
        schedules = schedules,
        requiresOcrReview = requiresOcrReview,
        reviewed = reviewed
    )

    private fun invoke(name: String = "Tratament", drafts: List<MedicationDraft>) =
        useCase(name, drafts, analysisDocumentIds = listOf("doc-1"), attachmentIds = listOf("att-1"))

    private fun BuildTreatmentResult.message() = (this as BuildTreatmentResult.Invalid).message

    @Test
    fun `blank treatment name is rejected`() {
        assertTrue(invoke(name = " ", drafts = listOf(draft())) is BuildTreatmentResult.Invalid)
    }

    @Test
    fun `empty drafts are rejected`() {
        assertTrue(invoke(drafts = emptyList()) is BuildTreatmentResult.Invalid)
    }

    @Test
    fun `blank medication name is rejected`() {
        assertTrue(invoke(drafts = listOf(draft(name = " "))) is BuildTreatmentResult.Invalid)
    }

    @Test
    fun `unreviewed ocr draft is rejected`() {
        val result = invoke(drafts = listOf(draft(requiresOcrReview = true, reviewed = false)))
        assertEquals("Verifică și confirmă fiecare medicament înainte de salvare.", result.message())
    }

    @Test
    fun `reviewed ocr draft is accepted`() {
        assertTrue(invoke(drafts = listOf(draft(requiresOcrReview = true, reviewed = true))) is BuildTreatmentResult.Valid)
    }

    @Test
    fun `non positive dose is rejected`() {
        assertEquals(
            "Doza trebuie să fie pozitivă sau necompletată.",
            invoke(drafts = listOf(draft(doseText = "0"))).message()
        )
    }

    @Test
    fun `dose accepts comma decimal and blank means unknown`() {
        val result = invoke(drafts = listOf(draft(doseText = "0,5"), draft(name = "B", doseText = "")))
        val medications = (result as BuildTreatmentResult.Valid).treatment.medications
        assertEquals(0.5, medications[0].doseValue!!, 0.0)
        assertNull(medications[1].doseValue)
    }

    @Test
    fun `duration outside 1 to 3650 is rejected`() {
        assertTrue(invoke(drafts = listOf(draft(durationText = "0"))) is BuildTreatmentResult.Invalid)
        assertTrue(invoke(drafts = listOf(draft(durationText = "3651"))) is BuildTreatmentResult.Invalid)
    }

    @Test
    fun `end date is start plus duration minus one`() {
        val result = invoke(drafts = listOf(draft(durationText = "5")))
        val medication = (result as BuildTreatmentResult.Valid).treatment.medications.single()
        assertEquals(LocalDate.of(2026, 1, 14), medication.endDate)
    }

    @Test
    fun `no duration means no end date`() {
        val medication = (invoke(drafts = listOf(draft())) as BuildTreatmentResult.Valid).treatment.medications.single()
        assertNull(medication.endDate)
    }

    @Test
    fun `duplicate administration times are rejected`() {
        val schedules = listOf(
            MedicationSchedule("08:00", MealRelation.NO_MEAL_RELATION),
            MedicationSchedule("08:00", MealRelation.AFTER_MEAL)
        )
        assertEquals(
            "Introdu ore distincte în format HH:mm.",
            invoke(drafts = listOf(draft(schedules = schedules))).message()
        )
    }

    @Test
    fun `invalid time format is rejected`() {
        val schedules = listOf(MedicationSchedule("25:00", MealRelation.NO_MEAL_RELATION))
        assertTrue(invoke(drafts = listOf(draft(schedules = schedules))) is BuildTreatmentResult.Invalid)
    }

    @Test
    fun `valid treatment carries every card, analysis ids and attachments`() {
        val result = invoke(
            name = " Tratament ",
            drafts = listOf(draft(name = "A", doseText = "1"), draft(name = "B"))
        ) as BuildTreatmentResult.Valid
        assertEquals("Tratament", result.treatment.name)
        assertEquals(listOf("A", "B"), result.treatment.medications.map { it.name })
        assertEquals(listOf("doc-1"), result.treatment.medications[0].analysisDocumentIds)
        assertEquals(listOf("att-1"), result.treatment.attachmentIds)
    }

    @Test
    fun `unit is passed through`() {
        val d = draft(name = "A").copy(doseUnit = DoseUnit.ML)
        val medication = (invoke(drafts = listOf(d)) as BuildTreatmentResult.Valid).treatment.medications.single()
        assertEquals(DoseUnit.ML, medication.doseUnit)
    }

    @Test
    fun `draft mealRelation is applied across every schedule`() {
        val schedules = listOf(
            MedicationSchedule("08:00", MealRelation.NO_MEAL_RELATION),
            MedicationSchedule("14:00", MealRelation.NO_MEAL_RELATION)
        )
        val d = draft(schedules = schedules).copy(mealRelation = MealRelation.AFTER_MEAL)
        val medication = (invoke(drafts = listOf(d)) as BuildTreatmentResult.Valid).treatment.medications.single()
        assertEquals(2, medication.schedules.size)
        assertTrue(medication.schedules.all { it.mealRelation == MealRelation.AFTER_MEAL })
    }
}
