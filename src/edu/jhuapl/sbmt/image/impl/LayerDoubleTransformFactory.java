package edu.jhuapl.sbmt.image.impl;

import java.util.List;
import java.util.function.Function;

import com.google.common.base.Preconditions;

import edu.jhuapl.sbmt.image.api.Layer;
import edu.jhuapl.sbmt.image.api.Pixel;
import edu.jhuapl.sbmt.image.api.PixelDouble;
import edu.jhuapl.sbmt.image.api.PixelVectorDouble;
import edu.jhuapl.sbmt.image.impl.LayerTransformFactory.ForwardingLayer;

/**
 * Factory class for creating {@link Layer} transforms (that is,
 * {@link Function} instances that operate on a layer and return a new layer).
 * <p>
 * This factory provides transforms that involve changes to the data associated
 * with a pixel. It can operate on layers that support pixels of type
 * {@link PixelDouble} and {@link PixelVectorDouble}.
 * <p>
 * The {@link Function#apply(Layer)} methods for all the functions returned by
 * this factory will return null if called with a null layer argument.
 *
 * @author James Peachey
 *
 */
public class LayerDoubleTransformFactory
{

    protected static final PixelDoubleFactory PixelScalarFactory = new PixelDoubleFactory();

    protected static final PixelVectorDoubleFactory PixelVectorFactory = new PixelVectorDoubleFactory();

    @FunctionalInterface
    public interface DoubleTransform
    {
        double apply(double value);
    }

    public static final DoubleTransform DoubleIdentity = value -> {
        return value;
    };

    public LayerDoubleTransformFactory()
    {
        super();
    }

    /**
     * Convert the kernel of a transform, (a {@link DoubleTransform}, which
     * operates on a scalar double value) into a {@link Function} that operates
     * on a {@link Layer} by transforming values within the pixel.
     * <p>
     * The base implementation handles {@link PixelDouble} and
     * {@link PixelVectorDouble} pixels. When overriding this method, take care
     * to ensure that out-of-bounds values are not used in computations, and
     * that the correct {@link DoubleTransform} instance is used for valid and
     * invalid values.
     *
     * @param valueTransform the transform to use on valid values
     * @param invalidValueTransform the transform to use on invalid values (if
     *            null, the regular valueTransform will be used for invalid
     *            values as well).
     * @return the layer-to-layer transform
     */
    public Function<Layer, Layer> toLayerTransform(DoubleTransform valueTransform, DoubleTransform invalidValueTransform)
    {
        Preconditions.checkNotNull(valueTransform);

        DoubleTransform finalInvalidValueTransform;
        if (invalidValueTransform == null)
        {
            finalInvalidValueTransform = valueTransform;
        }
        else
        {
            finalInvalidValueTransform = invalidValueTransform;
        }

        Function<Layer, Layer> function = layer -> {
            Preconditions.checkNotNull(layer);

            return new ForwardingLayer(layer) {

                @Override
                public void get(int i, int j, Pixel p)
                {
                    if (p instanceof PixelDouble)
                    {
                        PixelDouble pd = (PixelDouble) p;

                        // Make a copy of the pixel, and get its state from the
                        // layer.
                        PixelDouble tmpPd = PixelScalarFactory.of(pd);
                        layer.get(i, j, tmpPd);

                        boolean valid = tmpPd.isValid();
                        boolean inBounds = tmpPd.isInBounds();
                        double outOfBoundsValue = tmpPd.getOutOfBoundsValue();

                        // Handle all the special cases.
                        double value;
                        if (!inBounds)
                        {
                            // Do not transform an out-of-bounds value, ever.
                            // Pass through the canonical value.
                            value = outOfBoundsValue;
                        }
                        else if (valid)
                        {
                            // Value is in-bounds and valid, so use the regular
                            // value transform.
                            value = valueTransform.apply(tmpPd.getStoredValue());
                        }
                        else
                        {
                            // Value is not valid, so use the invalid value
                            // transform.
                            value = finalInvalidValueTransform.apply(tmpPd.getStoredValue());
                        }

                        pd.set(value);
                        pd.setIsValid(valid);
                        pd.setInBounds(inBounds);
                    }
                    else if (p instanceof PixelVectorDouble)
                    {
                        PixelVectorDouble pvd = (PixelVectorDouble) p;

                        // Make a copy of the pixel, and get its state from the
                        // layer.
                        PixelVectorDouble tmpPvd = PixelVectorFactory.of(pvd);
                        layer.get(i, j, tmpPvd);

                        boolean valid = tmpPvd.isValid();
                        boolean inBounds = tmpPvd.isInBounds();
                        double outOfBoundsValue = tmpPvd.getOutOfBoundsValue();
                        int kSize = tmpPvd.size();

                        for (int k = 0; k < kSize; ++k)
                        {
                            // Handle all the special cases.
                            double value;
                            if (!inBounds || !checkIndex(k, 0, kSize))
                            {
                                // Do not transform an out-of-bounds value,
                                // ever. Pass through the canonical value.
                                value = outOfBoundsValue;
                            }
                            else if (valid)
                            {
                                // Value is in-bounds and valid, so use the
                                // regular value transform.
                                value = valueTransform.apply(tmpPvd.get(k).getStoredValue());
                            }
                            else
                            {
                                // Value is not valid, so use the invalid value
                                // transform.
                                value = finalInvalidValueTransform.apply(tmpPvd.get(k).getStoredValue());
                            }

                            pvd.get(k).set(value);
                            pvd.setIsValid(valid);
                            pvd.setInBounds(inBounds);
                        }
                    }
                    else
                    {
                        throw new UnsupportedOperationException();
                    }
                }

            };

        };

        return function;
    }

    /**
     * Return a function that extracts one scalar slice from a vector layer.
     * This exposes a flaw in the interfaces. This operation should be possible
     * without caring about the type of pixel. See also
     * {@link LayerTransformFactory#resampleNearestNeighbor(int, int)}, which
     * should also not care about the nature of the pixels.
     *
     * @param index
     * @param outOfBoundsValue
     * @param invalidValue
     * @return
     */
    public Function<Layer, Layer> slice(int index, double outOfBoundsValue, Double invalidValue)
    {
        return layer -> {
            Preconditions.checkNotNull(layer);
            Preconditions.checkArgument(0 <= index);

            List<Integer> dataSizes = layer.dataSizes();
            Preconditions.checkNotNull(dataSizes);

            Integer size;
            if (dataSizes.isEmpty())
            {
                // Slicing a scalar layer is OK, though that will force index to
                // be 0 below.
                size = Integer.valueOf(1);
            }
            else
            {
                // Slicing a vector layer is OK.
                Preconditions.checkArgument(dataSizes.size() == 1);
                size = dataSizes.get(0);
            }

            // Confirm the layer has at least *some* data.
            Preconditions.checkNotNull(size);
            Preconditions.checkArgument(size > index);

            PixelVectorDouble p = PixelVectorFactory.of(size, outOfBoundsValue, invalidValue);

            return new BasicLayerOfDouble(layer.iSize(), layer.jSize()) {

                @Override
                protected double doGetDouble(int i, int j)
                {
                    layer.get(i, j, p);

                    return p.get(index).get();
                }

            };

        };
    }

    /**
     * Returns a function that resamples a layer to produce a layer of a new
     * size by associating each new layer (I, J) coordinate with a pixel that
     * has been interpolated from pixels in the original layer that lie around
     * the new coordinate.
     *
     * @param iNewSize the size of the output layer in the I dimension
     * @param jNewSize the size of the output layer in the J dimension
     * @return the function
     */
    public Function<Layer, Layer> linearInterpolate(int iNewSize, int jNewSize)
    {
        return layer -> {
            Preconditions.checkNotNull(layer);
            Preconditions.checkArgument(layer.isGetAccepts(PixelDouble.class));

            int iOrigSize = layer.iSize();
            int jOrigSize = layer.jSize();

            if (iOrigSize == iNewSize && jOrigSize == jNewSize)
            {
                return layer;
            }

            PixelDouble tmpPd = PixelScalarFactory.of(0., Double.NaN, Double.NaN);

            return new ResampledLayer(iNewSize, jNewSize) {

                @Override
                protected Layer getInputLayer()
                {
                    return layer;
                }

                @Override
                protected void getScalar(int iNew, int jNew, Pixel d)
                {
                    if (d instanceof PixelDouble pd)
                    {
                        // Get coordinates of the new pixel in the old pixel
                        // index space.
                        double x = (double) (iNew * iOrigSize) / iNewSize;
                        double y = (double) (jNew * jOrigSize) / jNewSize;

                        // Lower bounds in index space.
                        int i0 = (int) Math.floor(x);
                        int j0 = (int) Math.floor(y);

                        // Upper bounds in index space.
                        int i1 = i0 + 1;
                        int j1 = j0 + 1;

                        // Cast x and y into the range [0.0, 1.0).
                        x -= i0;
                        y -= j0;

                        // Get the 4 corner pixel values.
                        layer.get(i0, j0, tmpPd);
                        double pd00 = tmpPd.get();

                        layer.get(i0, j1, tmpPd);
                        double pd01 = tmpPd.get();

                        layer.get(i1, j0, tmpPd);
                        double pd10 = tmpPd.get();

                        layer.get(i1, j1, tmpPd);
                        double pd11 = tmpPd.get();

                        // Interpolate the two j0 corners, then the two j1
                        // corners.
                        double fy0 = interpolate(x, pd00, pd10);
                        double fy1 = interpolate(x, pd01, pd11);

                        // Interplate the two functions of y.
                        double fxy = interpolate(y, fy0, fy1);

                        if (Double.isFinite(fxy))
                        {
                            pd.set(fxy);
                            pd.setIsValid(true);
                        }
                        else
                        {
                            pd.setIsValid(false);
                        }
                    }
                    else
                    {
                        Preconditions.checkArgument(d instanceof PixelDouble);
                    }

                }

                /**
                 * Interpolate function values at either end of a function. If
                 * the x position, or either end point is not finite, it is not
                 * used.
                 *
                 * @param x position within a pixel, in the range [0.0, 1.0)
                 * @param fx0 value of the function when x == 0.0
                 * @param fx1 value of the function when y == 1.0
                 * @return fx interpolated at x position
                 */
                double interpolate(double x, double fx0, double fx1)
                {
                    if (Double.isFinite(x))
                    {
                        if (Double.isFinite(fx0) && Double.isFinite(fx1))
                        {
                            return (1.0 - x) * fx0 + x * fx1;
                        }
                        else if (Double.isFinite(fx0))
                        {
                            return fx0;
                        }
                        else if (Double.isFinite(fx1))
                        {
                            return fx1;
                        }
                    }

                    return Double.NaN;
                }
            };
        };
    }

    /**
     * Return a flag that indicates whether the specified index is in the
     * half-open range [minValue, maxValue).
     *
     * @param index the index value to check
     * @param minValid the minimum valid value for the index
     * @param maxValid one-past the maximum valid value for the index
     * @return true if the specified index is valid (in-bounds), false
     *         otherwise.
     */
    protected boolean checkIndex(int index, int minValid, int maxValid)
    {
        return index >= minValid && index < maxValid;
    }

}
