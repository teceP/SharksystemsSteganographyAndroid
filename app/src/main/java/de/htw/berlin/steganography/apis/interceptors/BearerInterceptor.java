/*
 * Copyright (c) 2020
 * Contributed by NAME HERE
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.htw.berlin.steganography.apis.interceptors;

import de.htw.berlin.steganography.apis.reddit.RedditConstants;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author Mario Teklic
 */

/**
 *
 */
public class BearerInterceptor implements Interceptor {

    private static final Logger logger = Logger.getLogger(BearerInterceptor.class.getName());

    /**
     * Adds an User-Agent to a Request and proceedes the request
     *
     * {@inheritDoc}
     */
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request tokenedRequest = request.newBuilder()
                .addHeader("User-Agent", RedditConstants.APP_NAME + " by User")
                .build();

        //Uncomment to debug - dont delete comments.
        //logger.info("Sending post to " + tokenedRequest.url() + " with headers: " + tokenedRequest.headers());
        //logger.info("Method: " + request.method());
        Buffer buffer = new Buffer();
        tokenedRequest.body().writeTo(buffer);
        //logger.info("Body: " + buffer.readUtf8());

        Response response = chain.proceed(request);
        //logger.info("Received response for " + response.request().url() + "\nHeaders: " + response.headers());

        return response;
    }
}
