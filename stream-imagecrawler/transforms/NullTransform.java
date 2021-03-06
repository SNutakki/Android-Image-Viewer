package edu.vanderbilt.imagecrawler.transforms;

import edu.vanderbilt.imagecrawler.utils.Image;

/**
 * The NullTransform will return the image as it was downloaded.  It's
 * purpose is to show the original image, as well as to exemplify how
 * transforms are supposed to work on a basic level.  It plays the role
 * of the "Concrete Component" in the Decorator pattern and the
 * "Concrete Class" in the Template Method pattern.
 */
public class NullTransform
		extends Transform {
    /**
     * Default constructor.
     */
    public NullTransform() {
        super();
    }

    /**
     * Constructors for the NullTransform. See GrayScaleTransform for
     * explanation of transform naming.
     */
    public NullTransform(String name) {
        super(name);
    }
	
    /**
     * Constructs a new Image that does not change the original at all.
     */
    @Override
    protected Image applyTransform(Image image) {
        return image;
    }
}
