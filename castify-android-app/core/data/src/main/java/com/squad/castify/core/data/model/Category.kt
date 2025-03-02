package com.squad.castify.core.data.model

import com.squad.castify.core.database.model.CategoryEntity
import com.squad.castify.core.network.model.NetworkCategory

fun NetworkCategory.asEntity() = CategoryEntity(
    id = id,
    name = name
)