/**
 * Copyright (C) 2021 DE4A
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.de4a.demoui;

import javax.annotation.concurrent.Immutable;

import com.helger.photon.security.mgr.PhotonSecurityManager;
import com.helger.photon.security.user.IUserManager;
import com.helger.photon.security.usergroup.IUserGroupManager;

@Immutable
public final class AppSecurity
{
  private AppSecurity ()
  {}

  public static void init ()
  {
    final IUserManager aUserMgr = PhotonSecurityManager.getUserMgr ();
    final IUserGroupManager aUserGroupMgr = PhotonSecurityManager.getUserGroupMgr ();

    // Standard users
    if (!aUserMgr.containsWithID (CApp.USER_ADMINISTRATOR_ID))
    {
      final boolean bDisabled = false;
      aUserMgr.createPredefinedUser (CApp.USER_ADMINISTRATOR_ID,
                                     CApp.USER_ADMINISTRATOR_LOGINNAME,
                                     CApp.USER_ADMINISTRATOR_EMAIL,
                                     CApp.USER_ADMINISTRATOR_PASSWORD,
                                     CApp.USER_ADMINISTRATOR_FIRSTNAME,
                                     CApp.USER_ADMINISTRATOR_LASTNAME,
                                     CApp.USER_ADMINISTRATOR_DESCRIPTION,
                                     CApp.USER_ADMINISTRATOR_LOCALE,
                                     CApp.USER_ADMINISTRATOR_CUSTOMATTRS,
                                     bDisabled);
    }

    // User group Administrators
    if (!aUserGroupMgr.containsWithID (CApp.USERGROUP_ADMINISTRATORS_ID))
    {
      aUserGroupMgr.createPredefinedUserGroup (CApp.USERGROUP_ADMINISTRATORS_ID,
                                               CApp.USERGROUP_ADMINISTRATORS_NAME,
                                               CApp.USERGROUP_ADMINISTRATORS_DESCRIPTION,
                                               CApp.USERGROUP_ADMINISTRATORS_CUSTOMATTRS);
      // Assign administrator user to administrators user group
      aUserGroupMgr.assignUserToUserGroup (CApp.USERGROUP_ADMINISTRATORS_ID, CApp.USER_ADMINISTRATOR_ID);
    }
  }
}
