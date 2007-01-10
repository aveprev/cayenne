package org.objectstyle.art.auto;

import java.util.List;

import org.objectstyle.art.Exhibit;
import org.objectstyle.art.Painting;
import org.objectstyle.cayenne.CayenneDataObject;

/** 
 * Class _Gallery was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually, 
 * since it may be overwritten next time code is regenerated. 
 * If you need to make any customizations, please use subclass. 
 */
public class _Gallery extends CayenneDataObject {

    public static final String GALLERY_NAME_PROPERTY = "galleryName";
    public static final String EXHIBIT_ARRAY_PROPERTY = "exhibitArray";
    public static final String PAINTING_ARRAY_PROPERTY = "paintingArray";

    public static final String GALLERY_ID_PK_COLUMN = "GALLERY_ID";

    public void setGalleryName(String galleryName) {
        writeProperty("galleryName", galleryName);
    }
    public String getGalleryName() {
        return (String)readProperty("galleryName");
    }
    
    
    public void addToExhibitArray(Exhibit obj) {
        addToManyTarget("exhibitArray", obj, true);
    }
    public void removeFromExhibitArray(Exhibit obj) {
        removeToManyTarget("exhibitArray", obj, true);
    }
    public List getExhibitArray() {
        return (List)readProperty("exhibitArray");
    }
    
    
    public void addToPaintingArray(Painting obj) {
        addToManyTarget("paintingArray", obj, true);
    }
    public void removeFromPaintingArray(Painting obj) {
        removeToManyTarget("paintingArray", obj, true);
    }
    public List getPaintingArray() {
        return (List)readProperty("paintingArray");
    }
    
    
}
