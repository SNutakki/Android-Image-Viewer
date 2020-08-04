package edu.vanderbilt.imagecrawler.crawlers;

import java.net.URL;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.vanderbilt.imagecrawler.crawlers.framework.ImageCrawler;
import edu.vanderbilt.imagecrawler.platform.Cache;
import edu.vanderbilt.imagecrawler.transforms.Transform;
import edu.vanderbilt.imagecrawler.utils.Array;
import edu.vanderbilt.imagecrawler.utils.ArrayCollector;
import edu.vanderbilt.imagecrawler.utils.Crawler;
import edu.vanderbilt.imagecrawler.utils.FuturesCollector;
import edu.vanderbilt.imagecrawler.utils.Image;

import static edu.vanderbilt.imagecrawler.utils.Crawler.Type.PAGE;
import static edu.vanderbilt.imagecrawler.utils.StreamsUtils.not;

/**
 * This implementation strategy uses Java 8 functional programming
 * features, the Java 8 completable futures framework, and the Java 8
 * sequential streams framework to perform an "image crawl" starting
 * from a root Uri.  Images from HTML page reachable from the root Uri
 * are downloaded from a remote web server or the local file system
 * and the results are stored in files that can be displayed to the
 * user.
 */
public class CompletableFuturesCrawler1
       extends ImageCrawler {
    /**
     * Stores a completed future with value of 0.
     */
    // TODO -- you fill in here (replace null with proper code).
    private CompletableFuture<Integer> mZero = CompletableFuture.completedFuture(0);

    /**
     * Perform the web crawl.
     *
     * @param pageUri The URL that we're crawling at this point
     * @param depth The current depth of the recursive processing
     * @return The number of images downloaded/stored.
     */
    protected int performCrawl(String pageUri,
                               int depth) {
        // Perform the crawl asynchronously, wait until all the
        // processing is done, and return the result.
        // TODO -- you fill in here (replace null with proper code).

        // waits until processing is done before returning so no async behavior observed
        return performCrawlAsync(pageUri, depth).join();
    }

    /**
     * Perform the web crawl by using completable futures to
     * asynchronously (1) download/store images on this page and (2)
     * crawl other hyperlinks accessible via this page.
     *
     * @param pageUri The URL that we're crawling at this point
     * @param depth The current depth of the recursive processing
     * @return A future to the the number of images downloaded/stored
     */
    private CompletableFuture<Integer> performCrawlAsync(String pageUri,
                                                         int depth) {
        log(">> Depth: " + depth + " [" + pageUri + "]" + " (" + Thread.currentThread().getId() + ")");

        // Return 0 if we've reached the depth limit of the web
        // crawling.
        if (depth > mMaxDepth) {
            log("Exceeded max depth of " + mMaxDepth);
            return mZero;
        }

        // Atomically check to see if we've already visited this URL
        // and add the new url to the cache we don't try to revisit
        // it again unnecessarily.
        else if (!mUniqueUris.putIfAbsent(pageUri)) {
            log("Already processed " + pageUri);

            // Return 0 if we've already examined this url.
            return mZero;
        } else 
            // Invoke the helper method.
            return performCrawlHelper(pageUri,
                                      depth);
    }

    /**
     * Perform the web crawl by using completable futures to
     * asynchronously (1) download/store images on this page and (2)
     * crawl other hyperlinks accessible via this page.
     *
     * @param pageUri The URL that we're crawling at this point
     * @param depth The current depth of the recursive processing
     * @return A future to the the number of images downloaded/stored
     */
    private CompletableFuture<Integer> performCrawlHelper(String pageUri,
                                                          int depth) {
        try {
            // Get a future to the HTML page associated with pageUri.
            CompletableFuture<Crawler.Page> pageFuture =
                getPageAsync(pageUri);

            // The following two asynchronous method calls run
            // concurrently in the fork-join thread pool.

            // After contents of the HTML page are obtained get a
            // future to the # of images processed on this page.
            CompletableFuture<Integer> imagesOnPageFuture =
                getImagesOnPageAsync(pageFuture);

            // After contents of the HTML page are obtained get a
            // future to the number of images processed on pages
            // linked from this page.
            CompletableFuture<Integer> imagesOnPageLinksFuture =
                crawlHyperLinksOnPageAsync(pageFuture,
                                           // Increment depth.
                                           depth + 1);

            // Return a completable future to the combined results of
            // the two futures params whenever they complete.
            return combineResultsAsync(imagesOnPageFuture,
                                       imagesOnPageLinksFuture);
        } catch (Exception e) {
            log("For '" + pageUri + "': " + e.getMessage());
            // Return completed future with value 0 if an exception
            // happens.  
            return mZero;
        }
    }

    /**
     * Asynchronously get the contents of the HTML page at {@code
     * pageUri}.
     *
     * @param pageUri The URL that we're crawling at this point
     * @return A completable future to the HTML page.
     */
    private CompletableFuture<Crawler.Page> getPageAsync(String pageUri) {
        // Load the HTML page asynchronously and return a completable
        // future to that page.
        // TODO -- you fill in here (replace null with proper code).

        return CompletableFuture.supplyAsync(() -> mWebPageCrawler.getPage(pageUri));
    }

    /**
     * Asynchronously download/store all the images in the page
     * associated with {@code pageFuture}.
     *
     * @param pageFuture A completable future to the page that's being
     *                   downloaded
     * @return A completable future to an integer containing the
     *         number of images downloaded/stored on this page
     */
    private CompletableFuture<Integer> getImagesOnPageAsync
        (CompletableFuture<Crawler.Page> pageFuture) {
        // Return a completable future to an integer containing the
        // number of images processed on this page.  This method
        // should call getImagesOnPage() and processImages()
        // asynchronously via a fluent chain of completion stage
        // methods.
        // TODO -- you fill in here (replace null with proper code).

        // waits until pageFuture finishes before obtaining and processing images
        return pageFuture
                .thenApplyAsync(this::getImagesOnPage)
                .thenComposeAsync(this::processImages);
    }

    /**
     * Asynchronously obtain a future to the number of images
     * processed on pages linked from this page.
     *
     * @param pageFuture A completable future to the page that's being
     *                   downloaded
     * @param depth The current depth of the recursive processing
     * @return A future to an integer containing # of images
     *         downloaded/stored on pages linked from this page
     */
    private CompletableFuture<Integer> 
        crawlHyperLinksOnPageAsync(CompletableFuture<Crawler.Page> pageFuture,
                                   int depth) {
        // Return a future to an integer containing the # of images
        // processed on pages linked from this page.  This method
        // should call crawlHyperLinksOnPage() asynchronously.
        // TODO -- you fill in here (replace null with proper code).

        // waits until pageFuture finishes before crawling through links
        return pageFuture.thenComposeAsync(finPage -> crawlHyperLinksOnPage(finPage, depth));
    }

    /**
     * Combines the results of two completable future parameters.
     *
     * @param imagesOnPageFuture A count of the number of images on a page
     * @param imagesOnPageLinksFuture an array of the counts of the
     *                                number of images on all pages
     *                                linked from a page
     * @return A completable future to the combined results of the two
     *         futures params after they complete
     */
    private CompletableFuture<Integer> combineResultsAsync
        (CompletableFuture<Integer> imagesOnPageFuture,
         CompletableFuture<Integer> imagesOnPageLinksFuture) {
        // Returns a completable future to the combined results of the
        // two futures params after they complete.
        // TODO -- you fill in here (replace null with proper code).

        // adds ints from 2 combined completable futures
        return imagesOnPageFuture.thenCombineAsync(imagesOnPageLinksFuture,Integer::sum);
    }

    /**
     * Recursively crawl through hyperlinks that are in a {@code
     * page}.
     *
     * @param page The page containing HTML
     * @param depth The depth of the level of web page traversal
     * @return A completable future to an integer that counts how many
     *         images were in each hyperlink on the page
     */
    private CompletableFuture<Integer> crawlHyperLinksOnPage(Crawler.Page page,
                                                             int depth) {
        // Return a completable future to an integer that counts the #
        // of nested hyperlinks accessible from the page.  This method
        // should contain one or more streams that use aggregate
        // operations (e.g., map(), collect(), and reduce()),
        // completable future methods (e.g., thenApply() and
        // FuturesCollector.toFuture()), and other methods (e.g.,
        // performCrawlAsync() and getPageElementsAsStrings()).
        // TODO -- you fill in here (replace null with proper code).

        // finishes when image counting is done
        return page.getPageElementsAsStrings(PAGE) // only images in hyperlinks from current page
                .stream()
                .map(pageURL -> performCrawlAsync(pageURL, depth)) // returns nested image count
                .reduce(this::combineResultsAsync) // adds image counts
                .orElse(mZero); // required to handle Optional returned from reduce
    }

    /**
     * Use Java 8 CompletableFutures and a sequential stream to
     * download, store, and transform images asynchronously.
     *
     * @param urls array of urls corresponding to images on the page
     * @return A completable future to an integer that counts how many
     * images were downloaded, stored, and transformed for all the
     * {@code urls} on the page
     */
    private CompletableFuture<Integer> processImages(Array<URL> urls) {
        // Return a completable future containing the # of images that
        // were downloaded, stored, and transformed.  This method
        // should contain one or more streams that use aggregate
        // operations (e.g., map(), collect(), flatMap(), and
        // count()), completable future methods (e.g., thenApply() and
        // FuturesCollector.toFuture()), and other methods (e.g.,
        // downloadAndStoreImageAsync() and transformImageAsync()).
        // TODO -- you fill in here (replace null with proper code).

        // future finishes when images are done being counted
        return CompletableFuture.supplyAsync(() -> {
            return (int)urls.stream()
                    // processes images asynchronously
                    .map(this::downloadAndStoreImageAsync)
                    .map(this::transformImageAsync)
                    .count();
        });
    }

    /**
     * Use Java 8 CompletableFutures and a sequential stream to
     * process the {@code image} by applying all the configured
     * transforms to it.
     *
     * @param imageFuture A future to an image that's being downloaded and stored
     * @return A completable future to an array of Images indicating the transform
     * operation(s) success or failure.
     */
    private CompletableFuture<Array<Image>> transformImageAsync
        (CompletableFuture<Image> imageFuture) {
        // Return a completable future to an array of Images that
        // indicate success/failure of the images that were processed
        // by applying the transforms in the mTransforms field.  This
        // method should contain completable future methods (e.g.,
        // thenApply() and FuturesCollector.toFuture()), stream
        // aggregate operations (e.g., filter(), map(), and
        // collect()), and other methods (e.g., createNewCacheItem()
        // and applyTransformAsync()).
        // TODO -- you fill in here (replace null with proper code).

        return imageFuture.thenApplyAsync(image -> {

            return mTransforms.stream()
                    // only transform non-cached images
                    .filter(transform -> createNewCacheItem(image, transform))
                    .map(transform -> applyTransformAsync(transform, image))
                    // wait for all transformations to finish before collecting into array
                    .map(CompletableFuture::join)
                    .collect(ArrayCollector.toArray());
        });
    }

    /**
     * Asynchronously download an image from the {@code url} parameter
     * and return a CompletableFuture that completes when the image
     * finishes being downloaded and stored in the cache.
     */
    private CompletableFuture<Image> downloadAndStoreImageAsync(URL url) {
        // Asynchronously download/store an Image from the url
        // parameter.
        return CompletableFuture.supplyAsync(() -> getOrDownloadImage(url));
    }

    /**
     * Apply the transform to the {@code image} asynchronously.
     *
     * @param image A downloaded image.
     * @return a future to a transformed image.
     */
    private CompletableFuture<Image> applyTransformAsync(Transform transform,
                                                         Image image) {

        // This method will only be called if a new empty cache entry
        // was created by a check to createNewCacheItem(). Get that
        // entry and pass it to the CachingTransformerDecorator.
        Cache.Item item =
                getCache().getItem(image.getSourceUrl().toString(),
                        transform.getName());

        // Asynchronously transform an image.
        return CompletableFuture
                .supplyAsync(() ->
                        makeTransformDecoratorWithImage(transform,
                                image)
                                .run(item));
    }
}
