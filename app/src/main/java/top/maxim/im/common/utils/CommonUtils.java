
package top.maxim.im.common.utils;

import android.content.Context;
import android.text.TextUtils;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import im.floo.floolib.BMXErrorCode;
import im.floo.floolib.BMXGroup;
import im.floo.floolib.BMXRosterItem;
import top.maxim.im.R;
import top.maxim.im.bmxmanager.BaseManager;
import top.maxim.im.bmxmanager.GroupManager;
import top.maxim.im.bmxmanager.UserManager;
import top.maxim.im.common.bean.UserBean;
import top.maxim.im.push.PushClientMgr;
import top.maxim.im.push.PushUtils;
import top.maxim.rtc.RTCManager;

import android.util.Base64;

import org.json.JSONObject;

/**
 * Description : 聊天工具类 Created by Mango on 2018/11/18.
 */
public class CommonUtils {

    private static CommonUtils mInstance;

    private static final String TAG = "CommonUtils";
    private static final Map<BMXErrorCode, Integer> mapCode2Msg = new HashMap<BMXErrorCode, Integer>() {
        {
            put(BMXErrorCode.InvalidParam, R.string.error_code_InvalidParam);
            put(BMXErrorCode.NotFound, R.string.error_code_NotFound);
            put(BMXErrorCode.DbOperationFailed, R.string.error_code_DbOperationFailed);
            put(BMXErrorCode.SignInInvalidParam, R.string.error_code_SignInInvalidParam);
            put(BMXErrorCode.SignInUserMapDbOperationFailed, R.string.error_code_SignInUserMapDbOperationFailed);
            put(BMXErrorCode.SignInUserDbOperationFailed, R.string.error_code_SignInUserDbOperationFailed);
            put(BMXErrorCode.SignInCancelled, R.string.error_code_SignInCancelled);
            put(BMXErrorCode.SignInTimeout, R.string.error_code_SignInTimeout);
            put(BMXErrorCode.SignInFailed, R.string.error_code_SignInFailed);
            put(BMXErrorCode.UserNotLogin, R.string.error_code_UserNotLogin);
            put(BMXErrorCode.UserAlreadyLogin, R.string.error_code_UserAlreadyLogin);
            put(BMXErrorCode.UserAuthFailed, R.string.error_code_UserAuthFailed);
            put(BMXErrorCode.UserPermissionDenied, R.string.error_code_UserPermissionDenied);
            put(BMXErrorCode.UserNotExist, R.string.error_code_UserNotExist);
            put(BMXErrorCode.UserAlreadyExist, R.string.error_code_UserAlreadyExist);
            put(BMXErrorCode.UserFrozen, R.string.error_code_UserFrozen);
            put(BMXErrorCode.UserBanned, R.string.error_code_UserBanned);
            put(BMXErrorCode.UserRemoved, R.string.error_code_UserRemoved);
            put(BMXErrorCode.UserTooManyDevice, R.string.error_code_UserTooManyDevice);
            put(BMXErrorCode.UserPasswordChanged, R.string.error_code_UserPasswordChanged);
            put(BMXErrorCode.UserKickedBySameDevice, R.string.error_code_UserKickedBySameDevice);
            put(BMXErrorCode.UserKickedByOtherDevices, R.string.error_code_UserKickedByOtherDevices);
            put(BMXErrorCode.UserAbnormal, R.string.error_code_UserAbnormal);
            put(BMXErrorCode.UserCancel, R.string.error_code_UserCancel);
            put(BMXErrorCode.UserOldPasswordNotMatch, R.string.error_code_UserOldPasswordNotMatch);
            put(BMXErrorCode.UserSigningIn, R.string.error_code_UserSigningIn);
            put(BMXErrorCode.PushTokenInvalid, R.string.error_code_PushTokenInvalid);
            put(BMXErrorCode.PushAliasBindByOtherUser, R.string.error_code_PushAliasBindByOtherUser);
            put(BMXErrorCode.PushAliasTokenNotMatch, R.string.error_code_PushAliasTokenNotMatch);
            put(BMXErrorCode.InvalidVerificationCode, R.string.error_code_InvalidVerificationCode);
            put(BMXErrorCode.InvalidRequestParameter, R.string.error_code_InvalidRequestParameter);
            put(BMXErrorCode.InvalidUserNameParameter, R.string.error_code_InvalidUserNameParameter);
            put(BMXErrorCode.MissingAccessToken, R.string.error_code_MissingAccessToken);
            put(BMXErrorCode.CurrentUserIsInRoster, R.string.error_code_CurrentUserIsInRoster);
            put(BMXErrorCode.CurrentUserIsInBlocklist, R.string.error_code_CurrentUserIsInBlocklist);
            put(BMXErrorCode.AnswerFailed, R.string.error_code_AnswerFailed);
            put(BMXErrorCode.InvalidToken, R.string.error_code_InvalidToken);
            put(BMXErrorCode.InvalidFileSign, R.string.error_code_InvalidFileSign);
            put(BMXErrorCode.InvalidFileObjectType, R.string.error_code_InvalidFileObjectType);
            put(BMXErrorCode.InvalidFileUploadToType, R.string.error_code_InvalidFileUploadToType);
            put(BMXErrorCode.InvalidFileDownloadUrl, R.string.error_code_InvalidFileDownloadUrl);
            put(BMXErrorCode.MessageInvalid, R.string.error_code_MessageInvalid);
            put(BMXErrorCode.MessageOutRecallTime, R.string.error_code_MessageOutRecallTime);
            put(BMXErrorCode.MessageRecallDisabled, R.string.error_code_MessageRecallDisabled);
            put(BMXErrorCode.MessageCensored, R.string.error_code_MessageCensored);
            put(BMXErrorCode.MessageInvalidType, R.string.error_code_MessageInvalidType);
            put(BMXErrorCode.MessageBadArg, R.string.error_code_MessageBadArg);
            put(BMXErrorCode.MessageRateLimitExceeded, R.string.error_code_MessageRateLimitExceeded);
            put(BMXErrorCode.RosterNotFriend, R.string.error_code_RosterNotFriend);
            put(BMXErrorCode.RosterBlockListExist, R.string.error_code_RosterBlockListExist);
            put(BMXErrorCode.RosterRejectApplication, R.string.error_code_RosterRejectApplication);
            put(BMXErrorCode.RosterHasDeletedFromSystem, R.string.error_code_RosterHasDeletedFromSystem);
            put(BMXErrorCode.GroupServerDbError, R.string.error_code_GroupServerDbError);
            put(BMXErrorCode.GroupNotExist, R.string.error_code_GroupNotExist);
            put(BMXErrorCode.GroupNotMemberFound, R.string.error_code_GroupNotMemberFound);
            put(BMXErrorCode.GroupMsgNotifyTypeUnknown, R.string.error_code_GroupMsgNotifyTypeUnknown);
            put(BMXErrorCode.GroupOwnerCannotLeave, R.string.error_code_GroupOwnerCannotLeave);
            put(BMXErrorCode.GroupTransferNotAllowed, R.string.error_code_GroupTransferNotAllowed);
            put(BMXErrorCode.GroupRecoveryMode, R.string.error_code_GroupRecoveryMode);
            put(BMXErrorCode.GroupExceedLimitGlobal, R.string.error_code_GroupExceedLimitGlobal);
            put(BMXErrorCode.GroupExceedLimitUserCreate, R.string.error_code_GroupExceedLimitUserCreate);
            put(BMXErrorCode.GroupExceedLimitUserJoin, R.string.error_code_GroupExceedLimitUserJoin);
            put(BMXErrorCode.GroupCapacityExceedLimit, R.string.error_code_GroupCapacityExceedLimit);
            put(BMXErrorCode.GroupMemberPermissionRequired, R.string.error_code_GroupMemberPermissionRequired);
            put(BMXErrorCode.GroupAdminPermissionRequired, R.string.error_code_GroupAdminPermissionRequired);
            put(BMXErrorCode.GroupOwnerPermissionRequired, R.string.error_code_GroupOwnerPermissionRequired);
            put(BMXErrorCode.GroupApplicationExpiredOrHandled, R.string.error_code_GroupApplicationExpiredOrHandled);
            put(BMXErrorCode.GroupInvitationExpiredOrHandled, R.string.error_code_GroupInvitationExpiredOrHandled);
            put(BMXErrorCode.GroupKickTooManyTimes, R.string.error_code_GroupKickTooManyTimes);
            put(BMXErrorCode.GroupMemberExist, R.string.error_code_GroupMemberExist);
            put(BMXErrorCode.GroupBlockListExist, R.string.error_code_GroupBlockListExist);
            put(BMXErrorCode.GroupAnnouncementNotFound, R.string.error_code_GroupAnnouncementNotFound);
            put(BMXErrorCode.GroupAnnouncementForbidden, R.string.error_code_GroupAnnouncementForbidden);
            put(BMXErrorCode.GroupSharedFileNotFound, R.string.error_code_GroupSharedFileNotFound);
            put(BMXErrorCode.GroupSharedFileOperateNotAllowed, R.string.error_code_GroupSharedFileOperateNotAllowed);
            put(BMXErrorCode.GroupMemberBanned, R.string.error_code_GroupMemberBanned);
            put(BMXErrorCode.ServerNotReachable, R.string.error_code_ServerNotReachable);
            put(BMXErrorCode.ServerUnknownError, R.string.error_code_ServerUnknownError);
            put(BMXErrorCode.ServerInvalid, R.string.error_code_ServerInvalid);
            put(BMXErrorCode.ServerDecryptionFailed, R.string.error_code_ServerDecryptionFailed);
            put(BMXErrorCode.ServerEncryptMethodUnsupported, R.string.error_code_ServerEncryptMethodUnsupported);
            put(BMXErrorCode.ServerBusy, R.string.error_code_ServerBusy);
            put(BMXErrorCode.ServerNeedRetry, R.string.error_code_ServerNeedRetry);
            put(BMXErrorCode.ServerTimeOut, R.string.error_code_ServerTimeOut);
            put(BMXErrorCode.ServerConnectFailed, R.string.error_code_ServerConnectFailed);
            put(BMXErrorCode.ServerDNSFailed, R.string.error_code_ServerDNSFailed);
            put(BMXErrorCode.ServerDNSFetchFailed, R.string.error_code_ServerDNSFetchFailed);
            put(BMXErrorCode.ServerDNSUserCancelFailed, R.string.error_code_ServerDNSUserCancelFailed);
            put(BMXErrorCode.ServerDNSParseDataFailed, R.string.error_code_ServerDNSParseDataFailed);
            put(BMXErrorCode.ServerDNSAppIdEmpty, R.string.error_code_ServerDNSAppIdEmpty);
            put(BMXErrorCode.ServerDNSAppIdInvalid, R.string.error_code_ServerDNSAppIdInvalid);
            put(BMXErrorCode.ServerDNSHealthCheckFailed, R.string.error_code_ServerDNSHealthCheckFailed);
            put(BMXErrorCode.ServerPrivateDNSParseDataFailed, R.string.error_code_ServerPrivateDNSParseDataFailed);
            put(BMXErrorCode.ServerTokenResponseInvalid, R.string.error_code_ServerTokenResponseInvalid);
            put(BMXErrorCode.ServerTokenRequestTooMany, R.string.error_code_ServerTokenRequestTooMany);
            put(BMXErrorCode.ServerTokenRequestParameterInvalid, R.string.error_code_ServerTokenRequestParameterInvalid);
            put(BMXErrorCode.ServerTokenRequestAppIdMissing, R.string.error_code_ServerTokenRequestAppIdMissing);
            put(BMXErrorCode.ServerTokenRequestAppIdInvalid, R.string.error_code_ServerTokenRequestAppIdInvalid);
            put(BMXErrorCode.ServerTokenAppStatusNotNormal, R.string.error_code_ServerTokenAppStatusNotNormal);
            put(BMXErrorCode.ServerNeedReconnected, R.string.error_code_ServerNeedReconnected);
            put(BMXErrorCode.ServerFileUploadUnknownError, R.string.error_code_ServerFileUploadUnknownError);
            put(BMXErrorCode.ServerFileDownloadUnknownError, R.string.error_code_ServerFileDownloadUnknownError);
            put(BMXErrorCode.ServerInvalidLicense, R.string.error_code_ServerInvalidLicense);
            put(BMXErrorCode.ServerLicenseLimit, R.string.error_code_ServerLicenseLimit);
            put(BMXErrorCode.ServerAppFrozen, R.string.error_code_ServerAppFrozen);
            put(BMXErrorCode.ServerTooManyRequest, R.string.error_code_ServerTooManyRequest);
            put(BMXErrorCode.ServerNotAllowOpenRegister, R.string.error_code_ServerNotAllowOpenRegister);
            put(BMXErrorCode.ServerFireplaceUnknownError, R.string.error_code_ServerFireplaceUnknownError);
            put(BMXErrorCode.ServerResponseInvalid, R.string.error_code_ServerResponseInvalid);
            put(BMXErrorCode.ServerInvalidUploadUrl, R.string.error_code_ServerInvalidUploadUrl);
            put(BMXErrorCode.ServerAppLicenseInvalid, R.string.error_code_ServerAppLicenseInvalid);
            put(BMXErrorCode.ServerAppLicenseExpired, R.string.error_code_ServerAppLicenseExpired);
            put(BMXErrorCode.ServerAppLicenseExceedLimit, R.string.error_code_ServerAppLicenseExceedLimit);
            put(BMXErrorCode.ServerAppIdMissing, R.string.error_code_ServerAppIdMissing);
            put(BMXErrorCode.ServerAppIdInvalid, R.string.error_code_ServerAppIdInvalid);
            put(BMXErrorCode.ServerAppSignInvalid, R.string.error_code_ServerAppSignInvalid);
            put(BMXErrorCode.ServerAppNotifierNotExist, R.string.error_code_ServerAppNotifierNotExist);
            put(BMXErrorCode.ServerNoClusterInfoForClusterId, R.string.error_code_ServerNoClusterInfoForClusterId);
            put(BMXErrorCode.ServerFileDownloadFailure, R.string.error_code_ServerFileDownloadFailure);
            put(BMXErrorCode.ServerAppStatusNotNormal, R.string.error_code_ServerAppStatusNotNormal);
            put(BMXErrorCode.ServerPlatformNotAllowed, R.string.error_code_ServerPlatformNotAllowed);
            put(BMXErrorCode.ServerCannotCreateDeviceSn, R.string.error_code_ServerCannotCreateDeviceSn);
            put(BMXErrorCode.ServerRtcNotOpen, R.string.error_code_ServerRtcNotOpen);
        }
    };

    private static final Map<BMXErrorCode, Integer> mapCode2Solution = new HashMap<BMXErrorCode, Integer>() {
        {
            put(BMXErrorCode.InvalidParam, R.string.error_solution_InvalidParam);
            put(BMXErrorCode.NotFound, R.string.error_solution_NotFound);
            put(BMXErrorCode.DbOperationFailed, R.string.error_solution_DbOperationFailed);
            put(BMXErrorCode.SignInInvalidParam, R.string.error_solution_SignInInvalidParam);
            put(BMXErrorCode.SignInUserMapDbOperationFailed, R.string.error_solution_SignInUserMapDbOperationFailed);
            put(BMXErrorCode.SignInUserDbOperationFailed, R.string.error_solution_SignInUserDbOperationFailed);
            put(BMXErrorCode.SignInCancelled, R.string.error_solution_SignInCancelled);
            put(BMXErrorCode.SignInTimeout, R.string.error_solution_SignInTimeout);
            put(BMXErrorCode.SignInFailed, R.string.error_solution_SignInFailed);
            put(BMXErrorCode.UserNotLogin, R.string.error_solution_UserNotLogin);
            put(BMXErrorCode.UserAlreadyLogin, R.string.error_solution_UserAlreadyLogin);
            put(BMXErrorCode.UserAuthFailed, R.string.error_solution_UserAuthFailed);
            put(BMXErrorCode.UserPermissionDenied, R.string.error_solution_UserPermissionDenied);
            put(BMXErrorCode.UserNotExist, R.string.error_solution_UserNotExist);
            put(BMXErrorCode.UserAlreadyExist, R.string.error_solution_UserAlreadyExist);
            put(BMXErrorCode.UserFrozen, R.string.error_solution_UserFrozen);
            put(BMXErrorCode.UserBanned, R.string.error_solution_UserBanned);
            put(BMXErrorCode.UserRemoved, R.string.error_solution_UserRemoved);
            put(BMXErrorCode.UserTooManyDevice, R.string.error_solution_UserTooManyDevice);
            put(BMXErrorCode.UserPasswordChanged, R.string.error_solution_UserPasswordChanged);
            put(BMXErrorCode.UserKickedBySameDevice, R.string.error_solution_UserKickedBySameDevice);
            put(BMXErrorCode.UserKickedByOtherDevices, R.string.error_solution_UserKickedByOtherDevices);
            put(BMXErrorCode.UserAbnormal, R.string.error_solution_UserAbnormal);
            put(BMXErrorCode.UserCancel, R.string.error_solution_UserCancel);
            put(BMXErrorCode.UserOldPasswordNotMatch, R.string.error_solution_UserOldPasswordNotMatch);
            put(BMXErrorCode.UserSigningIn, R.string.error_solution_UserSigningIn);
            put(BMXErrorCode.PushTokenInvalid, R.string.error_solution_PushTokenInvalid);
            put(BMXErrorCode.PushAliasBindByOtherUser, R.string.error_solution_PushAliasBindByOtherUser);
            put(BMXErrorCode.PushAliasTokenNotMatch, R.string.error_solution_PushAliasTokenNotMatch);
            put(BMXErrorCode.InvalidVerificationCode, R.string.error_solution_InvalidVerificationCode);
            put(BMXErrorCode.InvalidRequestParameter, R.string.error_solution_InvalidRequestParameter);
            put(BMXErrorCode.InvalidUserNameParameter, R.string.error_solution_InvalidUserNameParameter);
            put(BMXErrorCode.MissingAccessToken, R.string.error_solution_MissingAccessToken);
            put(BMXErrorCode.CurrentUserIsInRoster, R.string.error_solution_CurrentUserIsInRoster);
            put(BMXErrorCode.CurrentUserIsInBlocklist, R.string.error_solution_CurrentUserIsInBlocklist);
            put(BMXErrorCode.AnswerFailed, R.string.error_solution_AnswerFailed);
            put(BMXErrorCode.InvalidToken, R.string.error_solution_InvalidToken);
            put(BMXErrorCode.InvalidFileSign, R.string.error_solution_InvalidFileSign);
            put(BMXErrorCode.InvalidFileObjectType, R.string.error_solution_InvalidFileObjectType);
            put(BMXErrorCode.InvalidFileUploadToType, R.string.error_solution_InvalidFileUploadToType);
            put(BMXErrorCode.InvalidFileDownloadUrl, R.string.error_solution_InvalidFileDownloadUrl);
            put(BMXErrorCode.MessageInvalid, R.string.error_solution_MessageInvalid);
            put(BMXErrorCode.MessageOutRecallTime, R.string.error_solution_MessageOutRecallTime);
            put(BMXErrorCode.MessageRecallDisabled, R.string.error_solution_MessageRecallDisabled);
            put(BMXErrorCode.MessageCensored, R.string.error_solution_MessageCensored);
            put(BMXErrorCode.MessageInvalidType, R.string.error_solution_MessageInvalidType);
            put(BMXErrorCode.MessageBadArg, R.string.error_solution_MessageBadArg);
            put(BMXErrorCode.MessageRateLimitExceeded, R.string.error_solution_MessageRateLimitExceeded);
            put(BMXErrorCode.RosterNotFriend, R.string.error_solution_RosterNotFriend);
            put(BMXErrorCode.RosterBlockListExist, R.string.error_solution_RosterBlockListExist);
            put(BMXErrorCode.RosterRejectApplication, R.string.error_solution_RosterRejectApplication);
            put(BMXErrorCode.RosterHasDeletedFromSystem, R.string.error_solution_RosterHasDeletedFromSystem);
            put(BMXErrorCode.GroupServerDbError, R.string.error_solution_GroupServerDbError);
            put(BMXErrorCode.GroupNotExist, R.string.error_solution_GroupNotExist);
            put(BMXErrorCode.GroupNotMemberFound, R.string.error_solution_GroupNotMemberFound);
            put(BMXErrorCode.GroupMsgNotifyTypeUnknown, R.string.error_solution_GroupMsgNotifyTypeUnknown);
            put(BMXErrorCode.GroupOwnerCannotLeave, R.string.error_solution_GroupOwnerCannotLeave);
            put(BMXErrorCode.GroupTransferNotAllowed, R.string.error_solution_GroupTransferNotAllowed);
            put(BMXErrorCode.GroupRecoveryMode, R.string.error_solution_GroupRecoveryMode);
            put(BMXErrorCode.GroupExceedLimitGlobal, R.string.error_solution_GroupExceedLimitGlobal);
            put(BMXErrorCode.GroupExceedLimitUserCreate, R.string.error_solution_GroupExceedLimitUserCreate);
            put(BMXErrorCode.GroupExceedLimitUserJoin, R.string.error_solution_GroupExceedLimitUserJoin);
            put(BMXErrorCode.GroupCapacityExceedLimit, R.string.error_solution_GroupCapacityExceedLimit);
            put(BMXErrorCode.GroupMemberPermissionRequired, R.string.error_solution_GroupMemberPermissionRequired);
            put(BMXErrorCode.GroupAdminPermissionRequired, R.string.error_solution_GroupAdminPermissionRequired);
            put(BMXErrorCode.GroupOwnerPermissionRequired, R.string.error_solution_GroupOwnerPermissionRequired);
            put(BMXErrorCode.GroupApplicationExpiredOrHandled, R.string.error_solution_GroupApplicationExpiredOrHandled);
            put(BMXErrorCode.GroupInvitationExpiredOrHandled, R.string.error_solution_GroupInvitationExpiredOrHandled);
            put(BMXErrorCode.GroupKickTooManyTimes, R.string.error_solution_GroupKickTooManyTimes);
            put(BMXErrorCode.GroupMemberExist, R.string.error_solution_GroupMemberExist);
            put(BMXErrorCode.GroupBlockListExist, R.string.error_solution_GroupBlockListExist);
            put(BMXErrorCode.GroupAnnouncementNotFound, R.string.error_solution_GroupAnnouncementNotFound);
            put(BMXErrorCode.GroupAnnouncementForbidden, R.string.error_solution_GroupAnnouncementForbidden);
            put(BMXErrorCode.GroupSharedFileNotFound, R.string.error_solution_GroupSharedFileNotFound);
            put(BMXErrorCode.GroupSharedFileOperateNotAllowed, R.string.error_solution_GroupSharedFileOperateNotAllowed);
            put(BMXErrorCode.GroupMemberBanned, R.string.error_solution_GroupMemberBanned);
            put(BMXErrorCode.ServerNotReachable, R.string.error_solution_ServerNotReachable);
            put(BMXErrorCode.ServerUnknownError, R.string.error_solution_ServerUnknownError);
            put(BMXErrorCode.ServerInvalid, R.string.error_solution_ServerInvalid);
            put(BMXErrorCode.ServerDecryptionFailed, R.string.error_solution_ServerDecryptionFailed);
            put(BMXErrorCode.ServerEncryptMethodUnsupported, R.string.error_solution_ServerEncryptMethodUnsupported);
            put(BMXErrorCode.ServerBusy, R.string.error_solution_ServerBusy);
            put(BMXErrorCode.ServerNeedRetry, R.string.error_solution_ServerNeedRetry);
            put(BMXErrorCode.ServerTimeOut, R.string.error_solution_ServerTimeOut);
            put(BMXErrorCode.ServerConnectFailed, R.string.error_solution_ServerConnectFailed);
            put(BMXErrorCode.ServerDNSFailed, R.string.error_solution_ServerDNSFailed);
            put(BMXErrorCode.ServerDNSFetchFailed, R.string.error_solution_ServerDNSFetchFailed);
            put(BMXErrorCode.ServerDNSUserCancelFailed, R.string.error_solution_ServerDNSUserCancelFailed);
            put(BMXErrorCode.ServerDNSParseDataFailed, R.string.error_solution_ServerDNSParseDataFailed);
            put(BMXErrorCode.ServerDNSAppIdEmpty, R.string.error_solution_ServerDNSAppIdEmpty);
            put(BMXErrorCode.ServerDNSAppIdInvalid, R.string.error_solution_ServerDNSAppIdInvalid);
            put(BMXErrorCode.ServerDNSHealthCheckFailed, R.string.error_solution_ServerDNSHealthCheckFailed);
            put(BMXErrorCode.ServerPrivateDNSParseDataFailed, R.string.error_solution_ServerPrivateDNSParseDataFailed);
            put(BMXErrorCode.ServerTokenResponseInvalid, R.string.error_solution_ServerTokenResponseInvalid);
            put(BMXErrorCode.ServerTokenRequestTooMany, R.string.error_solution_ServerTokenRequestTooMany);
            put(BMXErrorCode.ServerTokenRequestParameterInvalid, R.string.error_solution_ServerTokenRequestParameterInvalid);
            put(BMXErrorCode.ServerTokenRequestAppIdMissing, R.string.error_solution_ServerTokenRequestAppIdMissing);
            put(BMXErrorCode.ServerTokenRequestAppIdInvalid, R.string.error_solution_ServerTokenRequestAppIdInvalid);
            put(BMXErrorCode.ServerTokenAppStatusNotNormal, R.string.error_solution_ServerTokenAppStatusNotNormal);
            put(BMXErrorCode.ServerNeedReconnected, R.string.error_solution_ServerNeedReconnected);
            put(BMXErrorCode.ServerFileUploadUnknownError, R.string.error_solution_ServerFileUploadUnknownError);
            put(BMXErrorCode.ServerFileDownloadUnknownError, R.string.error_solution_ServerFileDownloadUnknownError);
            put(BMXErrorCode.ServerInvalidLicense, R.string.error_solution_ServerInvalidLicense);
            put(BMXErrorCode.ServerLicenseLimit, R.string.error_solution_ServerLicenseLimit);
            put(BMXErrorCode.ServerAppFrozen, R.string.error_solution_ServerAppFrozen);
            put(BMXErrorCode.ServerTooManyRequest, R.string.error_solution_ServerTooManyRequest);
            put(BMXErrorCode.ServerNotAllowOpenRegister, R.string.error_solution_ServerNotAllowOpenRegister);
            put(BMXErrorCode.ServerFireplaceUnknownError, R.string.error_solution_ServerFireplaceUnknownError);
            put(BMXErrorCode.ServerResponseInvalid, R.string.error_solution_ServerResponseInvalid);
            put(BMXErrorCode.ServerInvalidUploadUrl, R.string.error_solution_ServerInvalidUploadUrl);
            put(BMXErrorCode.ServerAppLicenseInvalid, R.string.error_solution_ServerAppLicenseInvalid);
            put(BMXErrorCode.ServerAppLicenseExpired, R.string.error_solution_ServerAppLicenseExpired);
            put(BMXErrorCode.ServerAppLicenseExceedLimit, R.string.error_solution_ServerAppLicenseExceedLimit);
            put(BMXErrorCode.ServerAppIdMissing, R.string.error_solution_ServerAppIdMissing);
            put(BMXErrorCode.ServerAppIdInvalid, R.string.error_solution_ServerAppIdInvalid);
            put(BMXErrorCode.ServerAppSignInvalid, R.string.error_solution_ServerAppSignInvalid);
            put(BMXErrorCode.ServerAppNotifierNotExist, R.string.error_solution_ServerAppNotifierNotExist);
            put(BMXErrorCode.ServerNoClusterInfoForClusterId, R.string.error_solution_ServerNoClusterInfoForClusterId);
            put(BMXErrorCode.ServerFileDownloadFailure, R.string.error_solution_ServerFileDownloadFailure);
            put(BMXErrorCode.ServerAppStatusNotNormal, R.string.error_solution_ServerAppStatusNotNormal);
            put(BMXErrorCode.ServerPlatformNotAllowed, R.string.error_solution_ServerPlatformNotAllowed);
            put(BMXErrorCode.ServerCannotCreateDeviceSn, R.string.error_solution_ServerCannotCreateDeviceSn);
            put(BMXErrorCode.ServerRtcNotOpen, R.string.error_solution_ServerRtcNotOpen);
        }
    };

    private CommonUtils() {
    }

    public static CommonUtils getInstance() {
        if (mInstance == null) {
            synchronized (CommonUtils.class) {
                if (mInstance == null) {
                    mInstance = new CommonUtils();
                }
            }
        }
        return mInstance;
    }

    public void addUser(UserBean bean) {
        if (bean == null) {
            return;
        }
        // 添加登陆账号缓存
        Map<String, String> map = null;
        Gson gson = new Gson();
        String loginUserData = SharePreferenceUtils.getInstance().getLoginUserData();
        if (!TextUtils.isEmpty(loginUserData)) {
            map = gson.fromJson(loginUserData, Map.class);
        }
        if (map == null) {
            map = new HashMap<>();
        }
        map.put(String.valueOf(bean.getUserId()), gson.toJson(bean));
        SharePreferenceUtils.getInstance().putLoginUserData(gson.toJson(map));

        String userName = bean.getUserName();
        long userId = bean.getUserId();
        String pwd = bean.getUserPwd();
        SharePreferenceUtils.getInstance().putUserId(userId);
        SharePreferenceUtils.getInstance().putUserName(userName);
        SharePreferenceUtils.getInstance().putUserPwd(pwd);
    }

    public void logout() {
        SharePreferenceUtils.getInstance().putLoginStatus(false);
        SharePreferenceUtils.getInstance().putUserId(-1);
        SharePreferenceUtils.getInstance().putUserName("");
        SharePreferenceUtils.getInstance().putUserPwd("");
        SharePreferenceUtils.getInstance().putToken("");
        PushClientMgr.getManager().unRegister();
        PushUtils.getInstance().unregisterActivityListener(AppContextUtils.getApplication());
    }

    public void removeAccount(long id) {
        if (id <= 0) {
            return;
        }
        String loginUserData = SharePreferenceUtils.getInstance().getLoginUserData();
        Gson gson = new Gson();
        if (!TextUtils.isEmpty(loginUserData)) {
            Map<String, String> map = gson.fromJson(loginUserData, Map.class);
            String data = "";
            if (map != null && map.size() > 0) {
                map.remove(String.valueOf(id));
                if (map.size() > 0) {
                    data = gson.toJson(map);
                }
            }
            SharePreferenceUtils.getInstance().putLoginUserData(data);
        }
    }

    public List<UserBean> getLoginUsers() {
        String loginUserData = SharePreferenceUtils.getInstance().getLoginUserData();
        List<UserBean> beans = new ArrayList<>();
        Gson gson = new Gson();
        if (!TextUtils.isEmpty(loginUserData)) {
            Map<String, String> map = gson.fromJson(loginUserData, Map.class);
            if (map != null && map.size() > 0) {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    String userData = entry.getValue();
                    if (!TextUtils.isEmpty(userData)) {
                        beans.add(gson.fromJson(userData, UserBean.class));
                    }
                }
            }
        }
        if (!beans.isEmpty()) {
            Collections.sort(beans, new Comparator<UserBean>() {
                @Override
                public int compare(UserBean o1, UserBean o2) {
                    return o1.getTimestamp() > o2.getTimestamp() ? -1 : 0;
                }
            });
        }
        return beans;
    }

    public static void zipFolder(String sourceFolderPath, String zipFilePath) throws IOException {
        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFilePath));
        addFolderToZip("", sourceFolderPath, zipOutputStream);
        zipOutputStream.close();
    }

    private static void addFileToZip(String path, String srcFile, ZipOutputStream zipOutputStream) throws IOException {
        File folder = new File(srcFile);
        if (folder.isDirectory()) {
            addFolderToZip(path, srcFile, zipOutputStream);
        } else {
            byte[] buffer = new byte[1024];
            FileInputStream fileInputStream = new FileInputStream(srcFile);
            zipOutputStream.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
            int length;
            while ((length = fileInputStream.read(buffer)) > 0) {
                zipOutputStream.write(buffer, 0, length);
            }
            zipOutputStream.closeEntry();
            fileInputStream.close();
        }
    }

    private static void addFolderToZip(String path, String srcFolder, ZipOutputStream zipOutputStream) throws IOException {
        File folder = new File(srcFolder);
        for (String fileName : folder.list()) {
            if (path.equals("")) {
                addFileToZip(folder.getName(), srcFolder + "/" + fileName, zipOutputStream);
            } else {
                addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zipOutputStream);
            }
        }
    }

    public static String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }


    public static String md5InBase64(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = Base64.encodeToString(bytes, Base64.DEFAULT);
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean getAppConfigSwitch(String key) {
        boolean res = false;
        String config = BaseManager.getBMXClient().getSDKConfig().getAppConfig();
        if (!TextUtils.isEmpty(config)) {
            try {
                JSONObject jsonObject = new JSONObject(config);
                if (jsonObject.has(key)) {
                    res = jsonObject.getBoolean(key);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    public static int getAppConfigInteger(String key) {
        int res = 0;
        String config = BaseManager.getBMXClient().getSDKConfig().getAppConfig();
        if (!TextUtils.isEmpty(config)) {
            try {
                JSONObject jsonObject = new JSONObject(config);
                if (jsonObject.has(key)) {
                    res = jsonObject.getInt(key);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    public static String getAppConfigString(String key) {
        String res = "";
        String config = BaseManager.getBMXClient().getSDKConfig().getAppConfig();
        if (!TextUtils.isEmpty(config)) {
            try {
                JSONObject jsonObject = new JSONObject(config);
                if (jsonObject.has(key)) {
                    res = jsonObject.getString(key);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    public static int getErrorMessage(BMXErrorCode bmxErrorCode){
        return mapCode2Msg.get(bmxErrorCode);
    }

    public static int getErrorSolution(BMXErrorCode bmxErrorCode){
        return mapCode2Solution.get(bmxErrorCode);
    }

    public static String getGroupMemberDisplayName(BMXRosterItem item, long groupId, long userId, boolean hideMemberInfo) {
        String name = "";
        BMXGroup.Member member = GroupManager.getInstance().getMemberByDB(groupId, userId);
        if (item != null && !TextUtils.isEmpty(item.alias())) {
            name = item.alias();
        } else if (member != null && !TextUtils.isEmpty(member.getMGroupNickname())) {
            name = member.getMGroupNickname();
        } else if (item != null && !TextUtils.isEmpty(item.nickname())) {
            name = item.nickname();
        } else if (item != null) {
            name = item.username();
            if (hideMemberInfo){
                name = CommonUtils.md5InBase64(item.username()+String.valueOf(userId)).substring(0, 12);
            }
        }
        return name;
    }

    public static String getRosterDisplayName(BMXRosterItem rosterItem) {
        String name = "";
        if (rosterItem != null) {
            if (!TextUtils.isEmpty(rosterItem.alias())) {
                name = rosterItem.alias();
            } else if (!TextUtils.isEmpty(rosterItem.nickname())) {
                name = rosterItem.nickname();
            } else {
                name = rosterItem.username();
            }
        }
        return name;
    }

    public static String getCompanyName(Context context) {
        String companyName = CommonUtils.getAppConfigString("account_verification_name");
        String verificationtype = CommonUtils.getAppConfigString("account_verification_type");
        if (TextUtils.equals(verificationtype, "personal") && !TextUtils.isEmpty(companyName)){
            companyName = String.format("%s%s", context.getString(R.string.about_us_individual_developer), companyName);
        }
        if (TextUtils.isEmpty(companyName)){
            companyName = context.getString(R.string.about_us_unverified_developer);
        }
        return companyName;
    }

    public static String getVerificationStatusText(Context context) {
        String verificationStatus = CommonUtils.getAppConfigString("account_verification_status");
        Map<String, String> map = ImmutableMap.<String, String>builder()
                .put("unverified", context.getString(R.string.about_us_unverified))
                .put("verified", context.getString(R.string.about_us_verified))
                .put("expired", context.getString(R.string.about_us_expired))
                .put("","")
                .build();

        String verifyString = map.get(verificationStatus);
        return verifyString;
    }

    public static String getVerificationStatusChar(Context context) {
        String verificationStatus = CommonUtils.getAppConfigString("account_verification_status");
        Map<String, String> map = ImmutableMap.<String, String>builder()
                .put("unverified", "❓")
                .put("verified", "✅")
                .put("expired", "❗")
                .put("", "")
                .build();

        String verifyString = map.get(verificationStatus);
        return verifyString;
    }

    public static void initializeSDKAndAppId(){
        boolean initialized = SharePreferenceUtils.getInstance().hasSDKInitialized();
        if (!initialized){
            RTCManager.getInstance().init(AppContextUtils.getApplication(), BaseManager.getBMXClient());
            SharePreferenceUtils.getInstance().putSDKInitialized(true);
        }
        String appId = SharePreferenceUtils.getInstance().getAppId();
        UserManager.getInstance().changeAppId(appId, bmxErrorCode -> {});
    }

    public static void initializeSDK(){
        boolean initialized = SharePreferenceUtils.getInstance().hasSDKInitialized();
        if (!initialized){
            RTCManager.getInstance().init(AppContextUtils.getApplication(), BaseManager.getBMXClient());
            SharePreferenceUtils.getInstance().putSDKInitialized(true);
        }
    }

}
