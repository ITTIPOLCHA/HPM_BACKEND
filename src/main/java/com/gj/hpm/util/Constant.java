package com.gj.hpm.util;

import java.math.RoundingMode;

public class Constant {
        final public static String STATUS_ACTIVE = "A";

        final public static String ACTION_UPDATE = "UPDATE";
        final public static String ACTION_ADD = "ADD";

        final public static String EMPLOYEE_ID = "EMPLOYEE_ID";

        final public static String SALT = "@3wcdFw1#4WfVFtVDS";

        final public static String SECRET = "841D8A6C80CBA4FCAD32D5367C18C53B";

        final public static int REFCODE_LENGTH = 6;

        final public static int OTP_MAX_FAIL_COUNT = 3;

        final public static int OTP_EXPIRE_MINUTES = 5;

        final public static int QUOTA_LEAVE_SCALE = 2;

        final public static RoundingMode QUOTA_LEAVE_SCALE_UP = RoundingMode.UP;

        final public static double MINUTE_PER_SHIFT = 480;
        final public static String USER_LOGIN = "USER_LOGIN";

        public enum Key {
                email, username, password, mobileNumber, mobileCode, allowInitial, flagUser, line, user, picture, name;

                @Override
                public String toString() {
                        return this.name();
                }
        }

        public enum Language {
                en, th;

                @Override
                public String toString() {
                        return this.name();
                }
        }

        public enum TypeSignIn {
                line, email;

                @Override
                public String toString() {
                        return this.name();
                }
        }

        public enum ERole {
                ROLE_USER, ROLE_ADMIN;

                @Override
                public String toString() {
                        return this.name();
                }
        }

        public enum ApiReturn {
                CONTINUE("100", "CONTINUE", "Continue processing the request", "ดำเนินการต่อ"),
                SWITCHING_PROTOCOLS("101", "SWITCHING_PROTOCOLS", "Switching to a different protocol",
                                "กำลังเปลี่ยนไปใช้โปรโตคอลอื่น"),
                PROCESSING("102", "PROCESSING",
                                "The server is processing the request, but no response is available yet",
                                "เซิร์ฟเวอร์กำลังประมวลผลคำขอ แต่ยังไม่มีการตอบกลับ"),
                SUCCESS("200", "SUCCESS", "The request was successful", "คำขอสำเร็จ"),
                CREATED("201", "CREATED", "The request resulted in a new resource created", "สร้างข้อมูลสำเร็จ"),
                ACCEPTED("202", "ACCEPTED",
                                "The request has been accepted for processing, but the processing has not been completed",
                                "คำขอได้รับการยอมรับสำหรับการประมวลผล แต่การประมวลผลยังไม่เสร็จสมบูรณ์"),
                NON_AUTHORITATIVE_INFORMATION("203", "NON_AUTHORITATIVE_INFORMATION",
                                "The server is a transforming proxy that received 200 OK from its origin",
                                "เซิร์ฟเวอร์เป็นพร็อกซี่ที่ทำการแปลงและได้รับ 200 OK จากต้นฉบับของมัน"),
                NO_CONTENT("204", "NO_CONTENT",
                                "The server successfully processed the request but there is no content to return",
                                "เซิร์ฟเวอร์ประมวลผลคำขอเรียบร้อย แต่ไม่มีเนื้อหาที่จะส่งกลับ"),
                RESET_CONTENT("205", "RESET_CONTENT",
                                "The server successfully processed the request, but there is no content to return. Reset document view",
                                "เซิร์ฟเวอร์ประมวลผลคำขอเรียบร้อย แต่ไม่มีเนื้อหาที่จะส่งกลับ รีเซ็ตมุมมองเอกสาร"),
                PARTIAL_CONTENT("206", "PARTIAL_CONTENT",
                                "The server is delivering only part of the resource due to a range header sent by the client",
                                "เซิร์ฟเวอร์กำลังส่งมอบเพียงส่วนหนึ่งของทรัพยากรเนื่องจากเฮดเดอร์ของช่วงที่ถูกส่งมาจากไคลเอ็นต์"),
                MULTI_STATUS("207", "MULTI_STATUS", "Status for multiple independent operations",
                                "สถานะสำหรับการดำเนินการอิสระหลายรายการ"),
                ALREADY_REPORTED("208", "ALREADY_REPORTED",
                                "The members of a DAV binding have already been enumerated in a previous reply to this request, and are not being included again",
                                "สมาชิกของการผูก DAV ได้รับการนับแล้วในการตอบกลับก่อนหน้านี้และไม่ได้รับการรวมเข้า"),
                IM_USED("226", "IM_USED",
                                "The server has fulfilled a request for the resource, and the response is a representation of the result of one or more instance-manipulations applied to the current instance",
                                "เซิร์ฟเวอร์ได้บรรลุคำขอสำหรับทรัพยากร และการตอบกลับคือการแสดงผลของผลลัพธ์ของหนึ่งหรือมากกว่าการดำเนินการกับอินสแตนซ์ปัจจุบัน"),
                MULTIPLE_CHOICES("300", "MULTIPLE_CHOICES",
                                "Indicates that the requested URL has multiple representations",
                                "บ่งชี้ว่า URL ที่ขอมีการแสดงผลหลายอย่าง"),
                MOVED_PERMANENTLY("301", "MOVED_PERMANENTLY",
                                "The requested URL has been permanently moved to another location",
                                "URL ที่ขอได้ถูกย้ายถาวรไปยังตำแหน่งอื่น"),
                FOUND("302", "FOUND", "The requested URL has been temporarily moved to another location",
                                "URL ที่ขอได้ถูกย้ายชั่วคราวไปยังตำแหน่งอื่น"),
                SEE_OTHER("303", "SEE_OTHER", "The response to the request can be found under a different URL",
                                "การตอบกลับต่อคำขอสามารถพบได้ภายใต้ URL อื่น"),
                NOT_MODIFIED("304", "NOT_MODIFIED", "The resource has not been modified since the last request",
                                "ทรัพยากรยังไม่ได้ถูกแก้ไขตั้งแต่คำขอล่าสุด"),
                USE_PROXY("305", "USE_PROXY",
                                "The requested resource must be accessed through the proxy given by the Location field",
                                "ทรัพยากรที่ขอต้องถูกเข้าถึงผ่านพร็อกซี่ที่ระบุในฟิลด์ Location"),
                TEMPORARY_REDIRECT("307", "TEMPORARY_REDIRECT",
                                "The requested URL has been temporarily moved to another location",
                                "URL ที่ขอได้ถูกย้ายชั่วคราวไปยังตำแหน่งอื่น"),
                PERMANENT_REDIRECT("308", "PERMANENT_REDIRECT",
                                "The requested URL has been permanently moved to another location",
                                "URL ที่ขอได้ถูกย้ายถาวรไปยังตำแหน่งอื่น"),
                BAD_REQUEST("400", "BAD_REQUEST",
                                "The server cannot or will not process the request due to an apparent client error",
                                "เซิร์ฟเวอร์ไม่สามารถหรือไม่ต้องการประมวลผลคำขอเนื่องจากข้อผิดพลาดของไคลเอ็นต์ที่ชัดเจน"),
                UNAUTHORIZED("401", "UNAUTHORIZED",
                                "Authentication is required and has failed or has not been provided",
                                "จำเป็นต้องมีการตรวจสอบสิทธิและการตรวจสอบล้มเหลว หรือไม่ได้รับการให้สิทธิ"),
                PAYMENT_REQUIRED("402", "PAYMENT_REQUIRED", "Reserved for future use", "จองไว้สำหรับการใช้ในอนาคต"),
                FORBIDDEN("403", "FORBIDDEN", "The server understood the request, but refuses to authorize it",
                                "เซิร์ฟเวอร์เข้าใจคำขอ แต่ปฏิเสธการให้สิทธิ"),
                NOT_FOUND("404", "NOT_FOUND", "The requested resource could not be found on the server",
                                "ทรัพยากรที่ขอไม่พบบนเซิร์ฟเวอร์"),
                METHOD_NOT_ALLOWED("405", "METHOD_NOT_ALLOWED",
                                "The method specified in the request is not allowed for the resource identified by the request URI",
                                "วิธีที่ระบุในคำขอไม่ได้รับอนุญาตสำหรับทรัพยากรที่ระบุใน URI ของคำขอ"),
                NOT_ACCEPTABLE("406", "NOT_ACCEPTABLE",
                                "The server cannot produce a response matching the list of acceptable values defined in the request's headers",
                                "เซิร์ฟเวอร์ไม่สามารถสร้างคำตอบที่ตรงกับรายการค่าที่ยอมรับที่ถูกกำหนดไว้ในเฮดเดอร์ของคำขอ"),
                PROXY_AUTHENTICATION_REQUIRED("407", "PROXY_AUTHENTICATION_REQUIRED",
                                "Authentication is required with the proxy", "จำเป็นต้องมีการตรวจสอบสิทธิกับพร็อกซี่"),
                REQUEST_TIMEOUT("408", "REQUEST_TIMEOUT", "The server timed out waiting for the request",
                                "เซิร์ฟเวอร์หมดเวลารอคำขอ"),
                CONFLICT("409", "CONFLICT",
                                "The request could not be completed due to a conflict with the current state of the target resource",
                                "ไม่สามารถดำเนินการคำขอเนื่องจากข้อขัดแย้งกับสถานะปัจจุบันของทรัพยากรที่เป้าหมาย"),
                GONE("410", "GONE",
                                "The requested resource is no longer available at the server and no forwarding address is known",
                                "ทรัพยากรที่ขอไม่พร้อมให้บริการที่เซิร์ฟเวอร์และไม่มีที่อยู่ส่งต่อที่ทราบ"),
                LENGTH_REQUIRED("411", "LENGTH_REQUIRED",
                                "The server requires the request to be valid and have a content length",
                                "เซิร์ฟเวอร์ต้องการให้คำขอเป็นไปตามเงื่อนไขและมีความยาวของเนื้อหา"),
                PRECONDITION_FAILED("412", "PRECONDITION_FAILED",
                                "The server does not meet one of the preconditions specified in the request",
                                "เซิร์ฟเวอร์ไม่ตรงตามเงื่อนไขก่อนหน้าที่ระบุในคำขอ"),
                PAYLOAD_TOO_LARGE("413", "PAYLOAD_TOO_LARGE",
                                "The server refuses to process a request because the request payload is larger than the server is willing or able to process",
                                "เซิร์ฟเวอร์ปฏิเสธการประมวลผลคำขอเนื่องจากเนื้อหาของคำขอมีขนาดใหญ่กว่าที่เซิร์ฟเวอร์ยินดีที่จะประมวลผลหรือสามารถประมวลผลได้"),
                URI_TOO_LONG("414", "URI_TOO_LONG",
                                "The server refuses to service the request because the request-target is longer than the server is willing to interpret",
                                "เซิร์ฟเวอร์ปฏิเสธการให้บริการคำขอเนื่องจากเป้าหมายของคำขอยาวกว่าที่เซิร์ฟเวอร์ยินดีที่จะตีความ"),
                UNSUPPORTED_MEDIA_TYPE("415", "UNSUPPORTED_MEDIA_TYPE",
                                "The server refuses to service the request because the entity of the request is in a format not supported by the requested resource for the requested method",
                                "เซิร์ฟเวอร์ปฏิเสธการให้บริการคำขอเนื่องจากเอนทิตีของคำขออยู่ในรูปแบบที่ทรัพยากรที่ร้องขอไม่รองรับสำหรับวิธีที่ร้องขอ"),
                REQUESTED_RANGE_NOT_SATISFIABLE("416", "REQUESTED_RANGE_NOT_SATISFIABLE",
                                "The requested range is not suitable for the entity, or the entire entity is not available",
                                "ช่วงที่ร้องขอไม่เหมาะสมสำหรับเอนทิตี หรือทั้งเอนทิตีไม่พร้อมให้บริการ"),
                EXPECTATION_FAILED("417", "EXPECTATION_FAILED",
                                "The server cannot meet the requirements of the Expect request-header field",
                                "เซิร์ฟเวอร์ไม่สามารถตอบสนองตามความต้องการของเขตข้อมูล Expect ในเฮดเดอร์คำขอ"),
                I_AM_A_TEAPOT("418", "I_AM_A_TEAPOT",
                                "This code was defined in 1998 as one of the traditional IETF April Fools' jokes, in RFC 2324, Hyper Text Coffee Pot Control Protocol",
                                "รหัสนี้ถูกกำหนดในปี 1998 เป็นหนึ่งในตลกประจำเดือนเมษายนของ IETF ใน RFC 2324, Hyper Text Coffee Pot Control Protocol"),
                MISDIRECTED_REQUEST("421", "MISDIRECTED_REQUEST",
                                "The request was directed at a server that is not able to produce a response",
                                "คำขอถูกส่งไปที่เซิร์ฟเวอร์ที่ไม่สามารถสร้างคำตอบได้"),
                UNPROCESSABLE_ENTITY("422", "UNPROCESSABLE_ENTITY",
                                "The server understands the content type of the request entity and the syntax of the request entity is correct, but it was unable to process the contained instructions",
                                "เซิร์ฟเวอร์เข้าใจประเภทของเอนทิตีของคำขอและไวยากรณ์ของเอนทิตีของคำขอถูกต้อง แต่ไม่สามารถประมวลผลคำสั่งที่มีอยู่"),
                LOCKED("423", "LOCKED", "The source or destination resource of a method is locked",
                                "ทรัพยากรต้นฉบับหรือปลายทางของวิธีการถูกล็อก"),
                FAILED_DEPENDENCY("424", "FAILED_DEPENDENCY",
                                "The method could not be performed on the resource because the requested action depended on another action and that action failed",
                                "ไม่สามารถดำเนินการวิธีได้ในทรัพยากรเนื่องจากการดำเนินการที่ร้องขอต้องขึ้นอยู่กับการดำเนินการอื่นและการดำเนินการนั้นล้มเหลว"),
                UPGRADE_REQUIRED("426", "UPGRADE_REQUIRED", "The client should switch to a different protocol",
                                "ไคลเอ็นต์ควรสลับไปใช้โปรโตคอลอื่น"),
                PRECONDITION_REQUIRED("428", "PRECONDITION_REQUIRED",
                                "The origin server requires the request to be conditional",
                                "เซิร์ฟเวอร์ต้นฉบับต้องการให้คำขอมีเงื่อนไข"),
                TOO_MANY_REQUESTS("429", "TOO_MANY_REQUESTS",
                                "The user has sent too many requests in a given amount of time",
                                "ผู้ใช้ส่งคำขอมากเกินไปในช่วงเวลาที่กำหนด"),
                REQUEST_HEADER_FIELDS_TOO_LARGE("431", "REQUEST_HEADER_FIELDS_TOO_LARGE",
                                "The server is unwilling to process the request because its header fields are too large",
                                "เซิร์ฟเวอร์ไม่ยินดีที่จะประมวลผลคำขอเนื่องจากเฮดเดอร์ของคำขอมีขนาดใหญ่เกินไป"),
                UNAVAILABLE_FOR_LEGAL_REASONS("451", "UNAVAILABLE_FOR_LEGAL_REASONS",
                                "The server is denying access to the resource as a consequence of a legal demand",
                                "เซิร์ฟเวอร์ไม่ยินดีที่จะประมวลผลคำขอเนื่องจากเฮดเดอร์ของคำขอมีขนาดใหญ่เกินไป"),
                INTERNAL_SERVER_ERROR("500", "INTERNAL_SERVER_ERROR",
                                "A generic error message returned when an unexpected condition was encountered on the server",
                                "ข้อความข้อผิดพลาดทั่วไปที่ส่งกลับเมื่อเจอเงื่อนไขที่ไม่คาดคิดบนเซิร์ฟเวอร์"),
                NOT_IMPLEMENTED("501", "NOT_IMPLEMENTED",
                                "The server either does not recognize the request method or it lacks the ability to fulfill the request",
                                "เซิร์ฟเวอร์ไม่รู้จักวิธีการของคำขอ หรือไม่มีความสามารถในการปฏิบัติตามคำขอ"),
                BAD_GATEWAY("502", "BAD_GATEWAY",
                                "The server was acting as a gateway or proxy and received an invalid response from the upstream server",
                                "เซิร์ฟเวอร์กำลังทำหน้าที่เป็นเกตเวย์หรือพร็อกซี่และได้รับคำตอบที่ไม่ถูกต้องจากเซิร์ฟเวอร์ที่ตั้งอยู่ข้างบน"),
                SERVICE_UNAVAILABLE("503", "SERVICE_UNAVAILABLE", "The server is not ready to handle the request",
                                "เซิร์ฟเวอร์ไม่พร้อมให้บริการคำขอ"),
                GATEWAY_TIMEOUT("504", "GATEWAY_TIMEOUT",
                                "The server was acting as a gateway or proxy and did not receive a timely response from the upstream server",
                                "เซิร์ฟเวอร์กำลังทำหน้าที่เป็นเกตเวย์หรือพร็อกซี่และไม่ได้รับคำตอบทันเวลาจากเซิร์ฟเวอร์ที่ตั้งอยู่ข้างบน"),
                HTTP_VERSION_NOT_SUPPORTED("505", "HTTP_VERSION_NOT_SUPPORTED",
                                "The server does not support the HTTP protocol version that was used in the request message",
                                "เซิร์ฟเวอร์ไม่รองรับรุ่นโปรโตคอล HTTP ที่ถูกใช้ในข้อความคำขอ"),
                VARIANT_ALSO_NEGOTIATES("506", "VARIANT_ALSO_NEGOTIATES",
                                "Transparent content negotiation for the request, results in a circular reference",
                                "การต่อรองเนื้อหาโปร่งใสสำหรับคำขอ ทำให้เกิดการอ้างถึงรูปแบบวงกลม"),
                INSUFFICIENT_STORAGE("507", "INSUFFICIENT_STORAGE",
                                "The server is unable to store the representation needed to complete the request",
                                "เซิร์ฟเวอร์ไม่สามารถจัดเก็บการแสดงผลที่จำเป็นในการสมบูรณ์คำขอ"),
                LOOP_DETECTED("508", "LOOP_DETECTED", "The server detected an infinite loop while processing a request",
                                "เซิร์ฟเวอร์ตรวจพบลูปไร้ขอบเขตขณะประมวลผลคำขอ"),
                NOT_EXTENDED("510", "NOT_EXTENDED",
                                "Further extensions to the request are required for the server to fulfill it",
                                "ต้องมีการขยายต่อไปเพื่อให้เซิร์ฟเวอร์สามารถปฏิบัติตามมัน"),
                NETWORK_AUTHENTICATION_REQUIRED("511", "NETWORK_AUTHENTICATION_REQUIRED",
                                "The client needs to authenticate to gain network access",
                                "ไคลเอ็นต์ต้องตรวจสอบสิทธิเพื่อได้รับการเข้าถึงเครือข่าย");

                private final String code;
                private final String description;
                private final String detailEN;
                private final String detailTH;

                ApiReturn(String code, String description, String detailEN, String detailTH) {
                        this.code = code;
                        this.description = description;
                        this.detailEN = detailEN;
                        this.detailTH = detailTH;
                }

                public String code() {
                        return code;
                }

                public String description() {
                        return description;
                }

                public String detailEN() {
                        return detailEN;
                }

                public String detailTH() {
                        return detailTH;
                }
        }

        public enum FlagYN {
                Y, N
        }

        public enum Level {
                NORMAL("NORMAL", "Normal", "ปกติ"), WARNING("WARNING", "Warning", "คำเตือน"),
                DANGER("DANGER", "Danger", "อันตราย");

                private final String code;
                private final String valueEn;
                private final String valueTh;

                Level(String code, String valueEn, String valueTh) {
                        this.code = code;
                        this.valueEn = valueEn;
                        this.valueTh = valueTh;
                }

                @Override
                public String toString() {
                        return this.code;
                }

                public String code() {
                        return code;
                }

                public String valueEn() {
                        return valueEn;
                }

                public String valueTh() {
                        return valueTh;
                }

        }

        public enum StatusFlag {

                ACTIVE("ACTIVE", "Active", "ใช้งาน"), INACTIVE("INACTIVE", "Inactive", "ไม่ใช้งาน"),
                DELETE("DELETE", "Delete", "ถูกลบ");

                private final String code;
                private final String valueEn;
                private final String valueTh;

                StatusFlag(String code, String valueEn, String valueTh) {
                        this.code = code;
                        this.valueEn = valueEn;
                        this.valueTh = valueTh;
                }

                @Override
                public String toString() {
                        return this.code;
                }

                public String code() {
                        return code;
                }

                public String valueEn() {
                        return valueEn;
                }

                public String valueTh() {
                        return valueTh;
                }

        }

}