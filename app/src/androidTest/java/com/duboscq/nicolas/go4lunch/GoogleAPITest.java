package com.duboscq.nicolas.go4lunch;

import android.support.test.runner.AndroidJUnit4;

import com.duboscq.nicolas.go4lunch.api.APIStreams;
import com.duboscq.nicolas.go4lunch.models.restaurant.RestaurantDetail;
import com.duboscq.nicolas.go4lunch.models.restaurant.RestaurantPlace;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import static org.hamcrest.MatcherAssert.assertThat;


@RunWith(AndroidJUnit4.class)

public class GoogleAPITest {

    @Test
    public void googleNearAPITest() throws Exception {

        String key = "AIzaSyBiVX05PGFbUsnhdrcGX9UV0-xnTyv-PL4";
        double radius = 20;
        String location = "48.865075,2.353786";

        //1 - Get the stream
        Observable<RestaurantPlace> restaurantPlace = APIStreams.getRestaurantList(radius,key,location);
        //2 - Create a new TestObserver
        TestObserver<RestaurantPlace> testObserver = new TestObserver<>();
        //3 - Launch observable
        restaurantPlace.subscribeWith(testObserver)
                .assertNoErrors() // 3.1 - Check if no errors
                .assertNoTimeout() // 3.2 - Check if no Timeout
                .awaitTerminalEvent(); // 3.3 - Await the stream terminated before continue

        RestaurantPlace docs = testObserver.values().get(0);
        assertThat("ChIJL1X7ixpu5kcRHR7Ss0MYBtw", docs.getResults().get(0).getPlaceId().equals("ChIJL1X7ixpu5kcRHR7Ss0MYBtw") );
    }

    @Test
    public void googleDetailAPITest() throws Exception {

        String key = "AIzaSyBiVX05PGFbUsnhdrcGX9UV0-xnTyv-PL4";
        String placeid = "ChIJL1X7ixpu5kcRHR7Ss0MYBtw";

        //1 - Get the stream
        Observable<RestaurantDetail> restaurantDetail = APIStreams.getRestaurantDetail(placeid,key);
        //2 - Create a new TestObserver
        TestObserver<RestaurantDetail> testObserver = new TestObserver<>();
        //3 - Launch observable
        restaurantDetail.subscribeWith(testObserver)
                .assertNoErrors() // 3.1 - Check if no errors
                .assertNoTimeout() // 3.2 - Check if no Timeout
                .awaitTerminalEvent(); // 3.3 - Await the stream terminated before continue

        RestaurantDetail docs = testObserver.values().get(0);
        assertThat("ChIJL1X7ixpu5kcRHR7Ss0MYBtw", docs.getResult().getPlaceId().equals("ChIJL1X7ixpu5kcRHR7Ss0MYBtw") );
    }
}
