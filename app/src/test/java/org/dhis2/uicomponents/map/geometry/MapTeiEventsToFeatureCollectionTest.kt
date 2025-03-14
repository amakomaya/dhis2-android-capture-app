package org.dhis2.uicomponents.map.geometry

import com.mapbox.geojson.Point
import org.dhis2.maps.geometry.bound.GetBoundingBox
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeiEventsToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeiEventsToFeatureCollection.Companion.EVENT_UID
import org.dhis2.maps.geometry.point.MapPointToFeature
import org.dhis2.maps.geometry.polygon.MapPolygonToFeature
import org.dhis2.maps.model.MapItemModel
import org.dhis2.maps.model.RelatedInfo
import org.dhis2.ui.avatar.AvatarProviderConfiguration
import org.dhis2.uicomponents.map.mocks.GeometryDummy.getGeometryAsPoint
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.event.Event
import org.junit.Before
import org.junit.Test

class MapTeiEventsToFeatureCollectionTest {

    private lateinit var mapTeiEventsToFeatureCollection: MapTeiEventsToFeatureCollection

    @Before
    fun setUp() {
        mapTeiEventsToFeatureCollection =
            MapTeiEventsToFeatureCollection(
                mapPointToFeature = MapPointToFeature(),
                mapPolygonToFeature = MapPolygonToFeature(),
                bounds = GetBoundingBox(),
            )
    }

    @Test
    fun `Should map event models to point feature collection`() {
        val events = createEventsList()

        val result = mapTeiEventsToFeatureCollection.map(events)
        val featureCollection = result.first.featureCollectionMap

        featureCollection.values.forEach { it ->
            val resultGeometry = it.features()?.get(0)?.geometry() as Point
            val resultUid = it.features()?.get(0)?.getStringProperty(EVENT_UID)

            assertThat(resultUid, `is`(EVENTUID))
            assertThat(resultGeometry.longitude(), `is`(POINT_LONGITUDE))
            assertThat(resultGeometry.latitude(), `is`(POINT_LATITUDE))
        }
    }

    private fun createEventsList(): List<MapItemModel> {
        return listOf(
            MapItemModel(
                uid = EVENTUID,
                avatarProviderConfiguration = AvatarProviderConfiguration.ProfilePic(
                    "image",
                ),
                title = "",
                description = null,
                lastUpdated = "",
                additionalInfoList = emptyList(),
                isOnline = false,
                geometry = getGeometryAsPoint("[$POINT_LONGITUDE, $POINT_LATITUDE]"),
                relatedInfo = RelatedInfo(
                    event = RelatedInfo.Event(
                        stageUid = "stageUid",
                        stageDisplayName = "stage",
                        teiUid = "teiUid",
                    ),
                ),
                state = State.SYNCED,
            ),
        )
    }

    companion object {
        const val EVENTUID = "eventUid"
        const val POINT_LONGITUDE = 43.34532
        const val POINT_LATITUDE = -23.98234
    }
}
