import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import sanzol.app.config.AppProperties;
import sanzol.app.config.Version;
import sanzol.se.license.LicenseService;
import sanzol.util.properties.Property;
import sanzol.util.security.Cipher;
import sanzol.util.security.Cipher.Codec;
import sanzol.util.security.PasswordUtils;

public class License
{
	private StringBuilder sb;

	private void appendPropery(Property property, String value)
	{
		sb.append(property.getName() + "=" + value).append("\r\n");
	}

	public void generate()
	{
		sb = new StringBuilder();

		appendPropery(AppProperties.PROP_LICENSE_PRODUCT, Version.getVendor() + "." + Version.getProduct() + "_v" + Version.getVersionMajor());
		appendPropery(AppProperties.PROP_LICENSE_ID, "1");
		appendPropery(AppProperties.PROP_LICENSE_FINGERPRINT, "00000000000000000000000000000000");

		appendPropery(AppProperties.PROP_LICENSE_CLIENT, "anonymous");

		appendPropery(AppProperties.PROP_LICENSE_FROM, LocalDate.now(ZoneId.of("UTC")).atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
		appendPropery(AppProperties.PROP_LICENSE_TO, LocalDate.now(ZoneId.of("UTC")).atStartOfDay().plusMonths(18).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

		appendPropery(AppProperties.PROP_LICENSE_MODE, LicenseService.ValidateMode.ONLY_DATES.name());
		appendPropery(AppProperties.PROP_LICENSE_EXPIRED_ACTION, LicenseService.ExpiredAction.DELAY.name());

		System.out.println("");
		System.out.println(sb.toString());
		System.out.println("");
		System.out.println(Cipher.encrypt(sb.toString(), Codec.Hex));
	}

	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		System.out.println("--------------------------------------");
		System.out.println("------------ license.txt -------------");
		System.out.println("--------------------------------------");

		License fileLicense = new License();
		fileLicense.generate();

		System.out.println("--------------------------------------");
		System.out.println("");

		System.out.println("--------------------------------------");
		System.out.println("----------- table password -----------");
		System.out.println("--------------------------------------");
		System.out.println(PasswordUtils.hashPassword("admin", "aDmin..01"));
		System.out.println("--------------------------------------");
		System.out.println("");

		System.out.println("-------------------------------------");
		System.out.println("--------- config.properties ---------");
		System.out.println("-------------------------------------");
		System.out.println("base.username=***/" + Cipher.encrypt("appuser", Codec.Hex));
		System.out.println("base.password=***/" + Cipher.encrypt("m@sterKey..137", Codec.Hex));
		System.out.println("-------------------------------------");
		System.out.println("");
	}

}
