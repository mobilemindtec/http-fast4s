package io.http.fast4s.types

enum HttpStatus(val code: Int, val reason: String):
  case StatusCode(value: Int) extends HttpStatus(value, value.toString)
  case Continue extends HttpStatus(100, "Continue")
  case SwitchingProtocols extends HttpStatus(101, "Switching Protocols")
  case Processing extends HttpStatus(102, "Processing")
  case OK extends HttpStatus(200, "OK")
  case Created extends HttpStatus(201, "Created")
  case Accepted extends HttpStatus(202, "Accepted")
  case NonAuthoritativeInformation extends HttpStatus(203, "Non-authoritative Information")
  case NoContent extends HttpStatus(204, "No Content")
  case ResetContent extends HttpStatus(205, "Reset Content")
  case PartialContent extends HttpStatus(206, "Partial Content")
  case MultiStatus extends HttpStatus(207, "Multi-Status")
  case AlreadyReported extends HttpStatus(208, "Already Reported")
  case IMUsed extends HttpStatus(226, "IM Used")
  case MultipleChoices extends HttpStatus(300, "Multiple Choices")
  case MovedPermanently extends HttpStatus(301, "Moved Permanently")
  case Found extends HttpStatus(302, "Found")
  case SeeOther extends HttpStatus(303, "See Other")
  case NotModified extends HttpStatus(304, "Not Modified")
  case UseProxy extends HttpStatus(305, "Use Proxy")
  case TemporaryRedirect extends HttpStatus(307, "Temporary Redirect")
  case PermanentRedirect extends HttpStatus(308, "Permanent Redirect")
  case BadRequest extends HttpStatus(400, "Bad Request")
  case Unauthorized extends HttpStatus(401, "Unauthorized")
  case PaymentRequired extends HttpStatus(402, "Payment Required")
  case Forbidden extends HttpStatus(403, "Forbidden")
  case NotFound extends HttpStatus(404, "Not Found")
  case MethodNotAllowed extends HttpStatus(405, "Method Not Allowed")
  case NotAcceptable extends HttpStatus(406, "Not Acceptable")
  case ProxyAuthenticationRequired extends HttpStatus(407, "Proxy Authentication Required")
  case RequestTimeout extends HttpStatus(408, "Request Timeout")
  case Conflict extends HttpStatus(409, "Conflict")
  case Gone extends HttpStatus(410, "Gone")
  case LengthRequired extends HttpStatus(411, "Length Required")
  case PreconditionFailed extends HttpStatus(412, "Precondition Failed")
  case PayloadTooLarge extends HttpStatus(413, "Payload Too Large")
  case RequestURITooLong extends HttpStatus(414, "Request-URI Too Long")
  case UnsupportedMediaType extends HttpStatus(415, "Unsupported Media Type")
  case RequestedRangeNotSatisfiable extends HttpStatus(416, "Requested Range Not Satisfiable")
  case ExpectationFailed extends HttpStatus(417, "Expectation Failed")
  case ImAteapot extends HttpStatus(418, "I’m a teapot")
  case MisdirectedRequest extends HttpStatus(421, "Misdirected Request")
  case UnprocessableEntity extends HttpStatus(422, "Unprocessable Entity")
  case Locked extends HttpStatus(423, "Locked")
  case FailedDependency extends HttpStatus(424, "Failed Dependency")
  case UpgradeRequired extends HttpStatus(426, "Upgrade Required")
  case PreconditionRequired extends HttpStatus(428, "Precondition Required")
  case TooManyRequests extends HttpStatus(429, "Too Many Requests")
  case RequestHeaderFieldsTooLarge extends HttpStatus(431, "Request Header Fields Too Large")
  case ConnectionClosedWithoutResponse extends HttpStatus(444, "Connection Closed Without Response")
  case UnavailableForLegalReasons extends HttpStatus(451, "Unavailable For Legal Reasons")
  case ClientClosedRequest extends HttpStatus(499, "Client Closed Request")
  case InternalServerError extends HttpStatus(500, "Internal Server Error")
  case NotImplemented extends HttpStatus(501, "Not Implemented")
  case BadGateway extends HttpStatus(502, "Bad Gateway")
  case ServiceUnavailable extends HttpStatus(503, "Service Unavailable")
  case GatewayTimeout extends HttpStatus(504, "Gateway Timeout")
  case HTTPVersionNotSupported extends HttpStatus(505, "HTTP Version Not Supported")
  case VariantAlsoNegotiates extends HttpStatus(506, "Variant Also Negotiates")
  case InsufficientStorage extends HttpStatus(507, "Insufficient Storage")
  case LoopDetected extends HttpStatus(508, "Loop Detected")
  case NotExtended extends HttpStatus(510, "Not Extended")
  case NetworkAuthenticationRequired extends HttpStatus(511, "Network Authentication Required")
  case NetworkConnectTimeoutError extends HttpStatus(599, "Network Connect Timeout Error")

object HttpStatus:
  def make(code: Int): HttpStatus =
    code match
      case 100 => Continue
      case 101 => SwitchingProtocols
      case 102 => Processing
      case 200 => OK
      case 201 => Created
      case 202 => Accepted
      case 203 => NonAuthoritativeInformation
      case 204 => NoContent
      case 205 => ResetContent
      case 206 => PartialContent
      case 207 => MultiStatus
      case 208 => AlreadyReported
      case 226 => IMUsed
      case 300 => MultipleChoices
      case 301 => MovedPermanently
      case 302 => Found
      case 303 => SeeOther
      case 304 => NotModified
      case 305 => UseProxy
      case 307 => TemporaryRedirect
      case 308 => PermanentRedirect
      case 400 => BadRequest
      case 401 => Unauthorized
      case 402 => PaymentRequired
      case 403 => Forbidden
      case 404 => NotFound
      case 405 => MethodNotAllowed
      case 406 => NotAcceptable
      case 407 => ProxyAuthenticationRequired
      case 408 => RequestTimeout
      case 409 => Conflict
      case 410 => Gone
      case 411 => LengthRequired
      case 412 => PreconditionFailed
      case 413 => PayloadTooLarge
      case 414 => RequestURITooLong
      case 415 => UnsupportedMediaType
      case 416 => RequestedRangeNotSatisfiable
      case 417 => ExpectationFailed
      case 418 => ImAteapot
      case 421 => MisdirectedRequest
      case 422 => UnprocessableEntity
      case 423 => Locked
      case 424 => FailedDependency
      case 426 => UpgradeRequired
      case 428 => PreconditionRequired
      case 429 => TooManyRequests
      case 431 => RequestHeaderFieldsTooLarge
      case 444 => ConnectionClosedWithoutResponse
      case 451 => UnavailableForLegalReasons
      case 499 => ClientClosedRequest
      case 500 => InternalServerError
      case 501 => NotImplemented
      case 502 => BadGateway
      case 503 => ServiceUnavailable
      case 504 => GatewayTimeout
      case 505 => HTTPVersionNotSupported
      case 506 => VariantAlsoNegotiates
      case 507 => InsufficientStorage
      case 508 => LoopDetected
      case 510 => NotExtended
      case 511 => NetworkAuthenticationRequired
      case 599 => NetworkConnectTimeoutError
      case _ => StatusCode(code)