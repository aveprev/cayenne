package org.apache.cayenne.testdo.testmap.auto;

import java.math.BigDecimal;

import org.apache.cayenne.testdo.testmap.ArtDataObject;
import org.apache.cayenne.testdo.testmap.Artist;
import org.apache.cayenne.testdo.testmap.Gallery;
import org.apache.cayenne.testdo.testmap.PaintingInfo;

/**
 * Class _Painting was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _Painting extends ArtDataObject {

    public static final String ESTIMATED_PRICE_PROPERTY = "estimatedPrice";
    public static final String PAINTING_DESCRIPTION_PROPERTY = "paintingDescription";
    public static final String PAINTING_TITLE_PROPERTY = "paintingTitle";
    public static final String TO_ARTIST_PROPERTY = "toArtist";
    public static final String TO_GALLERY_PROPERTY = "toGallery";
    public static final String TO_PAINTING_INFO_PROPERTY = "toPaintingInfo";

    public static final String PAINTING_ID_PK_COLUMN = "PAINTING_ID";

    public void setEstimatedPrice(BigDecimal estimatedPrice) {
        writeProperty(ESTIMATED_PRICE_PROPERTY, estimatedPrice);
    }
    public BigDecimal getEstimatedPrice() {
        return (BigDecimal)readProperty(ESTIMATED_PRICE_PROPERTY);
    }

    public void setPaintingDescription(String paintingDescription) {
        writeProperty(PAINTING_DESCRIPTION_PROPERTY, paintingDescription);
    }
    public String getPaintingDescription() {
        return (String)readProperty(PAINTING_DESCRIPTION_PROPERTY);
    }

    public void setPaintingTitle(String paintingTitle) {
        writeProperty(PAINTING_TITLE_PROPERTY, paintingTitle);
    }
    public String getPaintingTitle() {
        return (String)readProperty(PAINTING_TITLE_PROPERTY);
    }

    public void setToArtist(Artist toArtist) {
        setToOneTarget(TO_ARTIST_PROPERTY, toArtist, true);
    }

    public Artist getToArtist() {
        return (Artist)readProperty(TO_ARTIST_PROPERTY);
    }


    public void setToGallery(Gallery toGallery) {
        setToOneTarget(TO_GALLERY_PROPERTY, toGallery, true);
    }

    public Gallery getToGallery() {
        return (Gallery)readProperty(TO_GALLERY_PROPERTY);
    }


    public void setToPaintingInfo(PaintingInfo toPaintingInfo) {
        setToOneTarget(TO_PAINTING_INFO_PROPERTY, toPaintingInfo, true);
    }

    public PaintingInfo getToPaintingInfo() {
        return (PaintingInfo)readProperty(TO_PAINTING_INFO_PROPERTY);
    }


}
