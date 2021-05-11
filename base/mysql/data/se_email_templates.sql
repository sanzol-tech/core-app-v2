INSERT INTO `se_email_templates` (`EMAIL_TEMPLATE_ID`,`NAME`,`TAGS`,`SUBJECT`,`PLAIN_BODY`,`HTML_BODY`) VALUES (1,'USER WELCOME','{name};{username};{password}','USER WELCOME','Welcome {name},\r Your user has been created !\r \r Username: {username}\r Password: {password}','Welcome {name},\r Your user has been created !\r \r Username: {username}\r Password: {password}');
INSERT INTO `se_email_templates` (`EMAIL_TEMPLATE_ID`,`NAME`,`TAGS`,`SUBJECT`,`PLAIN_BODY`,`HTML_BODY`) VALUES (2,'PASSWORD RESET','{name};{username};{password}','PASSWORD RESET','Hello {name},\nyour password was reset !\n\nUsername: {username}\nPassword: {password}','Hello {name},\nyour password was reset !\n\nUsername: {username}\nPassword: {password}');
INSERT INTO `se_email_templates` (`EMAIL_TEMPLATE_ID`,`NAME`,`TAGS`,`SUBJECT`,`PLAIN_BODY`,`HTML_BODY`) VALUES (3,'PASSWORD RECOVERY','{link};{validationCode};{expiration}','PASSWORD RECOVERY','Dear user!\n\nYou have used the password recovery procedure.\n\nTo obtain a new password, you must proceed to the link shown:\n\n{servletURL}/login/passwordRecovery.xhtml?vpCode={validationCode}\n\nValidation code: {validationCode}\nExpiration: {expiration}\n\nIf you have not applied for the recovery password procedure, please just ignore this letter. Your password will not be changed.\n','Dear user!\n\nYou have used the password recovery procedure.\n\nTo obtain a new password, you must proceed to the link shown:\n\n<a href=\"{servletURL}/login/passwordRecovery.xhtml?vpCode={validationCode}\">link</a>\n\nValidation code: {validationCode}\nExpiration: {expiration}\n\nIf you have not applied for the recovery password procedure, please just ignore this letter. Your password will not be changed.\n');
INSERT INTO `se_email_templates` (`EMAIL_TEMPLATE_ID`,`NAME`,`TAGS`,`SUBJECT`,`PLAIN_BODY`,`HTML_BODY`) VALUES (4,'PASSWORD CHANGED','{name};{username}','PASSWORD CHANGED','Hello {name},\n\nYour password has been changed !\n','Hello {name},\n\nYour password has been changed !\n');
INSERT INTO `se_email_templates` (`EMAIL_TEMPLATE_ID`,`NAME`,`TAGS`,`SUBJECT`,`PLAIN_BODY`,`HTML_BODY`) VALUES (5,'SIGN UP INVITATION','{name};{link};{validationCode};{expiration}','SIGN UP INVITATION','Hello {name},\n\n{servletURL}/login/userRegistration.xhtml?vpCode={validationCode}\n\nValidation code: {validationCode}\nExpiration: {expiration}\n','Hello {name},\n\n<a href=\"{servletURL}/login/userRegistration.xhtml?vpCode={validationCode}\">link</a>\n\nValidation code: {validationCode}\nExpiration: {expiration}\n');
INSERT INTO `se_email_templates` (`EMAIL_TEMPLATE_ID`,`NAME`,`TAGS`,`SUBJECT`,`PLAIN_BODY`,`HTML_BODY`) VALUES (6,'SIGN UP EMAIL VALIDATION','{name};{link};{validationCode};{expiration}','SIGN UP EMAIL VALIDATION','Hello {name},\n\n{servletURL}/login/userRegistration.xhtml?vpCode={validationCode}\n\nValidation code: {validationCode}\nExpiration: {expiration}\n','Hello {name},\n\n<a href=\"{servletURL}/login/userRegistration.xhtml?vpCode={validationCode}\">link</a>\n\nValidation code: {validationCode}\nExpiration: {expiration}\n');
INSERT INTO `se_email_templates` (`EMAIL_TEMPLATE_ID`,`NAME`,`TAGS`,`SUBJECT`,`PLAIN_BODY`,`HTML_BODY`) VALUES (7,'SIGN UP AURORIZATION PENDING','{name}','SIGN UP AURORIZATION PENDING','Hello {name},\n\nYour registration process is almost ready. \nWe have a few things to check and we\'ll let you know soon.','Hello {name},\n\nYour registration process is almost ready. \nWe have a few things to check and we\'ll let you know soon.');
INSERT INTO `se_email_templates` (`EMAIL_TEMPLATE_ID`,`NAME`,`TAGS`,`SUBJECT`,`PLAIN_BODY`,`HTML_BODY`) VALUES (8,'SIGN UP AURORIZATION SUCCESSFUL','{name};{link};{validationCode};{expiration}','SIGN UP AURORIZATION SUCCESSFUL','Hello {name},\n\nYour autorization is successful !\n\n{servletURL}/login/userRegistration.xhtml?vpCode={validationCode}\n\nValidation code: {validationCode}\nExpiration: {expiration}\n','Hello {name},\n\nYour autorization is successful !\n\n<a href=\"{servletURL}/login/userRegistration.xhtml?vpCode={validationCode}\">link</a>\n\nValidation code: {validationCode}\nExpiration: {expiration}\n');
INSERT INTO `se_email_templates` (`EMAIL_TEMPLATE_ID`,`NAME`,`TAGS`,`SUBJECT`,`PLAIN_BODY`,`HTML_BODY`) VALUES (9,'SIGN UP AURORIZATION REVOKED','{name}','SIGN UP AURORIZATION REVOKED','Hello {name},\n\nYour autorization is revoked !','Hello {name},\n\nYour autorization is revoked !');
INSERT INTO `se_email_templates` (`EMAIL_TEMPLATE_ID`,`NAME`,`TAGS`,`SUBJECT`,`PLAIN_BODY`,`HTML_BODY`) VALUES (10,'SIGN UP USER CREATED','{name}','SIGN UP USER CREATED','Welcome {name},\n\nYour registration was successful !\n','Welcome {name},\n\nYour registration was successful !\n');