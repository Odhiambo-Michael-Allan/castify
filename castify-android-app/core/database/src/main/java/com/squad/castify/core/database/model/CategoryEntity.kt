package com.squad.castify.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squad.castify.core.model.Category

@Entity(
    tableName = "categories"
)
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
)

fun CategoryEntity.asExternalModel() = Category(
    id = id,
    name = name
)
