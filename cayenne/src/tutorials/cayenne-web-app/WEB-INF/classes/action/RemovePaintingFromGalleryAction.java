package action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.objectstyle.cayenne.access.DataContext;
import org.objectstyle.cayenne.conf.ServletConfiguration;
import org.objectstyle.cayenne.exp.Expression;
import org.objectstyle.cayenne.exp.ExpressionFactory;
import org.objectstyle.cayenne.query.SelectQuery;

import webtest.Gallery;
import webtest.Painting;

public class RemovePaintingFromGalleryAction extends Action {

    public ActionForward execute(
        ActionMapping mapping,
        ActionForm form,
        HttpServletRequest request,
        HttpServletResponse response)
        throws Exception {

        DataContext ctxt =
            ServletConfiguration.getDefaultContext(request.getSession());

        String paintingTitle = request.getParameter("title");
        String galleryName = request.getParameter("galleryName");

        Expression qual = ExpressionFactory.matchExp("paintingTitle", paintingTitle);
        SelectQuery query = new SelectQuery(Painting.class, qual);

        List paintings = ctxt.performQuery(query);

        Painting painting = (Painting) paintings.get(0);
        System.err.println("painting: " + painting);

        qual = ExpressionFactory.matchExp("galleryName", galleryName);
        query = new SelectQuery(Gallery.class, qual);

        List galleries = ctxt.performQuery(query);
        Gallery gallery = (Gallery) galleries.get(0);

        gallery.removeFromPaintingArray(painting);

        // commit to the database
        ctxt.commitChanges();

        return mapping.findForward("success");
    }
}
