package com.squad.castify.core.datastore_test

import androidx.datastore.core.DataStoreFactory
import com.squad.castify.core.datastore.UserPreferencesSerializer
import kotlinx.coroutines.CoroutineScope
import org.junit.rules.TemporaryFolder

fun TemporaryFolder.testUserPreferencesDataStore(
    coroutineScope: CoroutineScope,
    userPreferencesSerializer: UserPreferencesSerializer = UserPreferencesSerializer()
) = DataStoreFactory.create(
    serializer = userPreferencesSerializer,
    scope = coroutineScope
) {
    newFile( "user_preferences_test.pb" )
}