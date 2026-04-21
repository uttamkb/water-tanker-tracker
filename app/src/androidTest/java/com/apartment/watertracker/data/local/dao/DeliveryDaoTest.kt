package com.apartment.watertracker.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.apartment.watertracker.data.local.WaterTrackerDatabase
import com.apartment.watertracker.data.local.entity.DeliveryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class DeliveryDaoTest {

    private lateinit var db: WaterTrackerDatabase
    private lateinit var dao: DeliveryDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, WaterTrackerDatabase::class.java
        ).build()
        dao = db.deliveryDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndReadDeliveries() = runBlocking {
        val delivery = DeliveryEntity(
            id = "del_1",
            apartmentId = "apt_1",
            vendorId = "ven_1",
            timestamp = Instant.now().toEpochMilli(),
            quantityLiters = 5000,
            driverName = "John Doe",
            status = "COMPLETED",
            latitude = 0.0,
            longitude = 0.0
        )

        dao.upsert(delivery)
        
        val deliveries = dao.observeDeliveriesByApartment("apt_1").first()
        
        assertEquals(1, deliveries.size)
        assertEquals("del_1", deliveries[0].id)
        assertEquals("John Doe", deliveries[0].driverName)
    }

    @Test
    fun deleteDelivery() = runBlocking {
        val delivery = DeliveryEntity(
            id = "del_1",
            apartmentId = "apt_1",
            vendorId = "ven_1",
            timestamp = Instant.now().toEpochMilli(),
            quantityLiters = 5000,
            driverName = "John Doe",
            status = "COMPLETED",
            latitude = 0.0,
            longitude = 0.0
        )

        dao.upsert(delivery)
        dao.deleteById("del_1")
        
        val deliveries = dao.observeDeliveriesByApartment("apt_1").first()
        
        assertEquals(0, deliveries.size)
    }
}
