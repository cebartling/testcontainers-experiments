package com.pintailconsultingllc.demo.entities

import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.Table
import java.util.*

@Table("races")
data class Race(
    @PrimaryKey
    val id: UUID,
    val name: String,
    val description: String,
    val date: Date
)
