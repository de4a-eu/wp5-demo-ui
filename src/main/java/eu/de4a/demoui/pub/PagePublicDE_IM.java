package eu.de4a.demoui.pub;

import javax.annotation.Nonnull;

import com.helger.commons.annotation.Nonempty;
import com.helger.html.hc.impl.HCNodeList;
import com.helger.photon.core.form.FormErrorList;
import com.helger.photon.uicore.page.WebPageExecutionContext;

import eu.de4a.demoui.ui.AbstractAppWebPage;

public class PagePublicDE_IM extends AbstractAppWebPage
{
  public PagePublicDE_IM (@Nonnull @Nonempty final String sID)
  {
    super (sID, "IM Exchange");
  }

  @Override
  protected void fillContent (final WebPageExecutionContext aWPEC)
  {
    final HCNodeList aNodeList = aWPEC.getNodeList ();

    final FormErrorList aFormErrors = new FormErrorList ();
  }
}
