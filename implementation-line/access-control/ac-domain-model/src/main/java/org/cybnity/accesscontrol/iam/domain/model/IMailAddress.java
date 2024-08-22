package org.cybnity.accesscontrol.iam.domain.model;

/**
 * Email address definition or criteria about an identity (e.g
 * company, person as subject of authorization).
 *
 * @author olivier
 */
public interface IMailAddress extends IdentityAttribute {

    enum Status {

        /**
         * Mail address recipient have been tested and confirmed as valid (usable).
         */
        VERIFIED
    }
}
