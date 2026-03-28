package com.semanticsoft.patientmobile.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.semanticsoft.patientmobile.data.local.db.PatientDatabase
import com.semanticsoft.patientmobile.data.local.db.entity.DocumentEntity
import com.semanticsoft.patientmobile.data.local.db.entity.SyncStatus
import com.semanticsoft.patientmobile.data.local.db.entity.UserEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DocumentDaoInstrumentedTest {

    private lateinit var db: PatientDatabase
    private lateinit var userDao: UserDao
    private lateinit var documentDao: DocumentDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PatientDatabase::class.java
        ).allowMainThreadQueries().build()

        userDao = db.userDao()
        documentDao = db.documentDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertRetrieveDelete_document() = runBlocking {
        userDao.insert(
            UserEntity(
                id = "u1",
                email = "john@example.com",
                firstName = "John",
                lastName = "Doe",
                dateOfBirth = "1990-01-01"
            )
        )

        val document = DocumentEntity(
            id = "d1",
            ownerUserId = "u1",
            originalFileName = "analysis.pdf",
            mimeType = "application/pdf",
            fileSizeBytes = 1024,
            uploadedAt = System.currentTimeMillis(),
            localFilePath = null,
            syncStatus = SyncStatus.SYNCED
        )

        documentDao.insert(document)

        val byId = documentDao.getDocumentById("d1")
        assertNotNull(byId)
        assertEquals("analysis.pdf", byId?.originalFileName)

        val all = documentDao.getAllDocuments()
        assertTrue(all.isNotEmpty())

        documentDao.deleteById("d1")
        val afterDelete = documentDao.getDocumentById("d1")
        assertEquals(null, afterDelete)
    }
}
