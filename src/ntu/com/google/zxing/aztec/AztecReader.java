/*
 * Copyright 2010 ZXing authors
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

package ntu.com.google.zxing.aztec;


import java.util.Hashtable;

import ntu.com.google.zxing.BarcodeFormat;
import ntu.com.google.zxing.BinaryBitmap;
import ntu.com.google.zxing.ChecksumException;
import ntu.com.google.zxing.DecodeHintType;
import ntu.com.google.zxing.FormatException;
import ntu.com.google.zxing.NotFoundException;
import ntu.com.google.zxing.Reader;
import ntu.com.google.zxing.Result;
import ntu.com.google.zxing.ResultMetadataType;
import ntu.com.google.zxing.ResultPoint;
import ntu.com.google.zxing.ResultPointCallback;
import ntu.com.google.zxing.aztec.decoder.Decoder;
import ntu.com.google.zxing.aztec.detector.Detector;
import ntu.com.google.zxing.common.DecoderResult;

/**
 * This implementation can detect and decode Aztec codes in an image.
 *
 * @author David Olivier
 */
public final class AztecReader implements Reader {

  /**
   * Locates and decodes a Data Matrix code in an image.
   *
   * @return a String representing the content encoded by the Data Matrix code
   * @throws NotFoundException if a Data Matrix code cannot be found
   * @throws FormatException if a Data Matrix code cannot be decoded
   * @throws ChecksumException if error correction fails
   */
  public Result decode(BinaryBitmap image) throws NotFoundException, FormatException {
    return decode(image, null);
  }

  public Result decode(BinaryBitmap image, Hashtable hints)
      throws NotFoundException, FormatException {

    AztecDetectorResult detectorResult = new Detector(image.getBlackMatrix()).detect();
    ResultPoint[] points = detectorResult.getPoints();

    if (hints != null && detectorResult.getPoints() != null) {
      ResultPointCallback rpcb = (ResultPointCallback) hints.get(DecodeHintType.NEED_RESULT_POINT_CALLBACK);
      if (rpcb != null) {
        for (int i = 0; i < detectorResult.getPoints().length; i++) {
          rpcb.foundPossibleResultPoint(detectorResult.getPoints()[i]);
        }
      }
    }

    DecoderResult decoderResult = new Decoder().decode(detectorResult);

    Result result = new Result(decoderResult.getText(), decoderResult.getRawBytes(), points, BarcodeFormat.AZTEC);
    
    if (decoderResult.getByteSegments() != null) {
      result.putMetadata(ResultMetadataType.BYTE_SEGMENTS, decoderResult.getByteSegments());
    }
    if (decoderResult.getECLevel() != null) {
      result.putMetadata(ResultMetadataType.ERROR_CORRECTION_LEVEL, decoderResult.getECLevel().toString());
    }
    
    return result;
  }

  public void reset() {
    // do nothing
  }

}