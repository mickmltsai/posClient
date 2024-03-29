/*
 * Copyright 2007 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ntu.com.google.zxing.client.result;

import ntu.com.google.zxing.BarcodeFormat;
import ntu.com.google.zxing.Result;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link EmailAddressParsedResult}.
 *
 * @author Sean Owen
 */
public final class EmailAddressParsedResultTestCase extends Assert {

  @Test
  public void testEmailAddress() {
    doTest("srowen@example.org", "srowen@example.org", null, null);
    doTest("mailto:srowen@example.org", "srowen@example.org", null, null);
  }

  @Test
  public void testEmailDocomo() {
    doTest("MATMSG:TO:srowen@example.org;;", "srowen@example.org", null, null);
    doTest("MATMSG:TO:srowen@example.org;SUB:Stuff;;", "srowen@example.org", "Stuff", null);
    doTest("MATMSG:TO:srowen@example.org;SUB:Stuff;BODY:This is some text;;", "srowen@example.org",
        "Stuff", "This is some text");
  }

  @Test
  public void testSMTP() {
    doTest("smtp:srowen@example.org", "srowen@example.org", null, null);
    doTest("SMTP:srowen@example.org", "srowen@example.org", null, null);
    doTest("smtp:srowen@example.org:foo", "srowen@example.org", "foo", null);
    doTest("smtp:srowen@example.org:foo:bar", "srowen@example.org", "foo", "bar");
  }

  private static void doTest(String contents, String email, String subject, String body) {
    Result fakeResult = new Result(contents, null, null, BarcodeFormat.QR_CODE);
    ParsedResult result = ResultParser.parseResult(fakeResult);
    assertSame(ParsedResultType.EMAIL_ADDRESS, result.getType());
    EmailAddressParsedResult emailResult = (EmailAddressParsedResult) result;
    assertEquals(email, emailResult.getEmailAddress());
    assertEquals("mailto:" + email, emailResult.getMailtoURI());
    assertEquals(subject, emailResult.getSubject());
    assertEquals(body, emailResult.getBody());
  }

}