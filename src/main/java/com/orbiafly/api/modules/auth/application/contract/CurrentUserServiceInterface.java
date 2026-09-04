package com.orbiafly.api.modules.auth.application.contract;

import com.orbiafly.api.modules.user.domain.User;

public interface CurrentUserServiceInterface {

  User getCurrentUser();
}
