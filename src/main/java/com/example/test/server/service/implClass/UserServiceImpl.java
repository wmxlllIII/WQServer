package com.example.test.server.service.implClass;


import com.example.test.common.constant.JwtClaimsConstant;
import com.example.test.common.constant.MessageConstant;
import com.example.test.common.context.BaseContext;
import com.example.test.common.enums.AuthType;
import com.example.test.common.enums.EventType;
import com.example.test.common.enums.FriendStatuType;
import com.example.test.common.enums.UpdateUserType;
import com.example.test.common.exception.*;
import com.example.test.common.properties.JwtProperties;
import com.example.test.common.result.PageResult;
import com.example.test.common.result.Result;
import com.example.test.common.utils.*;
import com.example.test.pojo.dto.*;
import com.example.test.pojo.entity.*;
import com.example.test.pojo.vo.*;
import com.example.test.server.mapper.UserMapper;
import com.example.test.server.service.EmailService;
import com.example.test.server.service.MessagePushService;
import com.example.test.server.service.UserService;
import com.example.test.server.websocket.WebSocketServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final VerifyUtil verifyUtil;
    private final EmailService emailService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MessagePushService messagePushService;

    @Autowired
    private JwtProperties jwtProperties;


    @Override
    public void getCode(SendCodeDTO sendCodeDTO) {

        String email = sendCodeDTO.getEmail();

        if (!verifyUtil.isEmail(email)) {
            throw new InvalidEmailException(MessageConstant.EMAIL_INVALID);
        }

        try {
            String code = verifyUtil.generateCode();
            log.info("code:  {}", code);
            verifyUtil.storeCode(email, code);
            emailService.sendVerificationCode(email, code);
        } catch (Exception e) {
            throw new SendException("[x] getCode #75");
        }
    }


    @Override
    public User register(RegisterDTO registerDTO) {
        String email = registerDTO.getEmail();
        String code = registerDTO.getCode();

        if (!verifyUtil.isEmail(email)) {
            throw new InvalidEmailException(MessageConstant.EMAIL_INVALID);
        }

        if (!verifyUtil.isRightCode(email, code)) {
            throw new InvalidVerificationCodeException(MessageConstant.ERROR_CODE);
        }


        System.out.println("===============================验证码通过");


        Auth auth = userMapper.getAuth(AuthType.EMAIL.getValue(), email);
        if (auth != null) {
            throw new DuplicateEmailException(MessageConstant.ALREADY_REGISTED);
        }

        //TODO 复杂密码验证

        User user = new User();
        Auth loginAuth = new Auth();
        try {
            // 生成唯一UUNumber（YYMMDDHHmm+3位毫秒）
            long timestamp = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
            String timePart = sdf.format(new Date(timestamp));
            String millisPart = String.format("%03d", timestamp % 1000);
            long uuNumber = Long.parseLong(timePart + millisPart);

            user.setUuNumber(uuNumber);
            user.setUsername("WQ_" + uuNumber);
            user.setAvatarUrl("");

            loginAuth.setUserId(uuNumber);
            loginAuth.setAuthType(AuthType.EMAIL.getValue());
            loginAuth.setAuthValue(email);
            loginAuth.setPassword(DigestUtils.md5DigestAsHex(registerDTO.getPassword().getBytes()));

            userMapper.insertUser(user);
            userMapper.insertAuth(loginAuth);

            verifyUtil.deleteCode(email);

        } catch (Exception e) {
            throw new BaseException(MessageConstant.UNKNOWN_ERROR);
        }
        return userMapper.getByUuNumber(user.getUuNumber());
    }


    @Override
    public User login(UserLoginDTO userLoginDTO) {
        int authType = userLoginDTO.getAuthType();
        String authValue = userLoginDTO.getAuthValue();
        String password = userLoginDTO.getPassword();

        Auth auth = userMapper.getAuth(authType, authValue);
        if (auth == null) {
            throw new UserNotFoundException(MessageConstant.PASSWORD_ERROR);
        }

        User user = userMapper.getByUuNumber(auth.getUserId());
        if (user == null) {
            throw new UserNotFoundException(MessageConstant.PASSWORD_ERROR);
        }

        password = DigestUtils.md5DigestAsHex(password.getBytes());

        log.info("Md5加密密码:{}", password);
        if (!password.equals(auth.getPassword())) {
            throw new InvalidPasswordException(MessageConstant.PASSWORD_ERROR);
        }
        return user;
    }


    @Override
    public UserLoginVO autoLogin() {
        long currentId = BaseContext.getCurrentId();
        User user = userMapper.getByUuNumber(currentId);
        if (user == null) {
            throw new UserNotFoundException(MessageConstant.PASSWORD_ERROR);
        }

        Auth authById = userMapper.getAuthById(AuthType.EMAIL.getValue(), user.getUuNumber());

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, user.getUuNumber());

        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .name(user.getUsername())
                .email(authById.getAuthValue())
                .uuNumber(user.getUuNumber())
                .avatarUrl(UrlUtil.fillUrl(user.getAvatarUrl()))
                .token(token)
                .build();
        log.info("userLoginVO:{}", userLoginVO);

        return userLoginVO;
    }

    @Override
    public UserVO updateAvatar(AvatarUploadDTO avatarUploadDTO) {
        long currentId = BaseContext.getCurrentId();
        MultipartFile file = avatarUploadDTO.getFile();

        if (file == null || file.isEmpty()) {
            throw new InvalidAvatarException("上传文件不能为空");
        }

        String contentType = avatarUploadDTO.getFile().getContentType();
        if (!Arrays.asList("image/jpeg", "image/png").contains(contentType)) {
            throw new InvalidAvatarException("仅支持JPG/PNG格式");
        }

        try {
            User oldUser = userMapper.getByUuNumber(currentId); // 获取旧用户信息
            String oldAvatarUrl = oldUser.getAvatarUrl();

            String uploadDir = "C:/avatar/";
            String urlPrefix = "/avatar/";
            String avatarUrl = saveFileToDisk(file, uploadDir, urlPrefix);
            //TODO 更新数据库中用户头像的路径

            log.info("currentId:{}", currentId);
            userMapper.updateAvatar(currentId, avatarUrl);

            if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
                deleteOldAvatarFile(oldAvatarUrl);
            }

            User user = userMapper.getByUuNumber(currentId);
            Auth authById = userMapper.getAuthById(AuthType.EMAIL.getValue(), user.getUuNumber());
            Auth authByPhone = userMapper.getAuthById(AuthType.PHONE.getValue(), user.getUuNumber());
            return UserVO.builder()
                    .username(user.getUsername())
                    .avatarUrl(UrlUtil.fillUrl(avatarUrl))
                    .email(authById.getAuthValue())
                    .phone(authByPhone.getAuthValue())
                    .uuNumber(currentId)
                    .createAt(TimeUtil.dateTimeToSecond(user.getCreateAt()))
                    .updateAt(TimeUtil.dateTimeToSecond(user.getUpdateAt()))
                    .build();
        } catch (Exception e) {
            log.info("[x] 头像更新失败MessageConstant.UNKNOWN_ERROR #220" + e.getMessage());
            throw new BaseException(MessageConstant.UNKNOWN_ERROR + "头像更新失败");
        }
    }

    private String saveFileToDisk(MultipartFile file, String uploadDir, String urlPrefix) throws IOException {


        String originalName = file.getOriginalFilename();
        String extension = originalName.substring(originalName.lastIndexOf("."));
        String fileName = UUID.randomUUID() + extension;


        Path targetPath = Paths.get(uploadDir + fileName);

        //  自动创建目录（如果不存在）内部封装了检查目录已经存在
        Files.createDirectories(targetPath.getParent());

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }


        log.info("fileName:{}", fileName);
        return urlPrefix + fileName;
    }

    private void deleteOldAvatarFile(String oldAvatarUrl) {
        try {
            String baseDir = "C:/avatar/";
            String fileName = oldAvatarUrl.replace("/avatar/", "");
            Path path = Paths.get(baseDir + fileName);

            if (Files.exists(path)) {
                Files.delete(path);
                log.info("删除旧头像: {}", path);
            }
        } catch (IOException e) {
            throw new BaseException("无法删除旧头像");
        }
    }


    @Override
    public SearchUserVO searchUser(SearchUserDTO searchUserDTO) {
        String phone = searchUserDTO.getPhone();
        long uuNumber = searchUserDTO.getUuNumber();
        String email = searchUserDTO.getEmail();
        User user = new User();
        if (!phone.isEmpty()) {
            Auth auth = userMapper.getAuth(AuthType.PHONE.getValue(), phone);
            if (auth == null) {
                throw new UserNotFoundException("用户不存在");
            }

            user = userMapper.getByUuNumber(auth.getUserId());
        }

        if (!email.isEmpty()) {
            Auth auth = userMapper.getAuth(AuthType.EMAIL.getValue(), email);
            if (auth == null) {
                throw new UserNotFoundException("用户不存在");
            }

            user = userMapper.getByUuNumber(auth.getUserId());
        }

        if (uuNumber > 0) {
            user = userMapper.getByUuNumber(uuNumber);
        }

        if (user == null) {
            log.info("用户不存在");
            return null;
        }

        FriendInfoVO friendInfoVO = FriendInfoVO.builder()
                .uuNumber(user.getUuNumber())
                .username(user.getUsername())
                .avatarUrl(UrlUtil.fillUrl(user.getAvatarUrl()))
                .updateAt(TimeUtil.dateTimeToSecond(user.getUpdateAt()))
                .build();
        SearchUserVO searchUserVO = SearchUserVO.builder()
                .FriendInfoVO(friendInfoVO)
                .isFriend(true)
                .isInBlackList(true)
                .build();

        return searchUserVO;
    }

    @Override
    public String FriendApply(FriendApplyDTO friendApplyDTO) {
        long currentId = BaseContext.getCurrentId();
        long targetId = friendApplyDTO.getTargetId();
        User receiver = userMapper.getByUuNumber(targetId);

        if (userMapper.existsPendingApply(currentId, targetId)) {
            throw new BaseException("请勿重复申请");
        }

        User sender = userMapper.getByUuNumber(currentId);

        FriendRelationship relationship = new FriendRelationship();
        relationship.setStatus(FriendStatuType.PENDING.getValue());
        relationship.setReceiverId(targetId);
        relationship.setSenderId(currentId);
        relationship.setValidMsg(friendApplyDTO.getValidMsg());

        userMapper.saveFriReq(relationship);

        //在线直接推送，不在线存离线表
        if (WebSocketServer.isUserOnline(targetId)) {
            FriendRelationship relation = userMapper.getTargetRela(currentId, targetId);
            List<Map<String, Object>> requestList = new ArrayList<>();
            Map<String, Object> request = BuildRelaUtil.buildRequest(sender, receiver, relation);
            requestList.add(request);
            messagePushService.pushToUser(targetId, EventType.EVENT_TYPE_REQUEST_FRIEND, requestList);
        }

        return "sended";
    }

    @Override
    public boolean deleteFriend(DeleteFriendDTO dto) {
        long currentId = BaseContext.getCurrentId();
        if (dto.getUserId() <= 0 || dto.getUserId() == currentId) {
            return false;
        }

        return userMapper.deleteFriend(currentId, dto.getUserId());
    }

    @Override
    public HandleFriendRequestVO handleResponse(HandleFriendRequestDTO friendRequestDTO) {
        long currentId = BaseContext.getCurrentId();
        long sourceUuNumber = friendRequestDTO.getSourceUuNumber();//申请者
        boolean isAgree = friendRequestDTO.isAgree();
        log.info("sourceUuNumber {}", sourceUuNumber);
        log.info("isAgree {}", isAgree);
        User requester = userMapper.getByUuNumber(sourceUuNumber);
        Auth auth = userMapper.getAuthById(AuthType.EMAIL.getValue(), sourceUuNumber);

        if (isAgree) {
            log.info("isAgree enter");
            userMapper.updateStateAgree(requester.getUuNumber(), currentId);
            userMapper.insertFriend(requester.getUuNumber(), currentId);
        } else {
            log.info("! isAgree ");
            userMapper.updateStateReject(requester.getUuNumber(), currentId);
        }
        HandleFriendRequestVO vo = new HandleFriendRequestVO();
        FriendRelationship targetRela = userMapper.getTargetRela(sourceUuNumber, currentId);
        vo.setFriendRelationship(targetRela.toVO());

        if (isAgree) {
            UserVO userVO = new UserVO();
            userVO.setUsername(requester.getUsername());
            userVO.setAvatarUrl(UrlUtil.fillUrl(requester.getAvatarUrl()));
            userVO.setEmail(auth.getAuthValue());
            userVO.setUuNumber(requester.getUuNumber());
            userVO.setUpdateAt(-1L);
            vo.setUser(userVO);
        }

        return vo;
    }

    @Override
    public List<Map<String, Object>> getAllFriendRequest() {
        long currentId = BaseContext.getCurrentId();
        List<FriendRelationship> relationshipList = userMapper.getAllRela(currentId);

        List<Map<String, Object>> requestList = new ArrayList<>();
        if (!relationshipList.isEmpty()) {

            for (FriendRelationship relation : relationshipList) {
                long senderId = relation.getSenderId();
                long receiverId = relation.getReceiverId();
                User sender = userMapper.getByUuNumber(senderId);
                User receiver = userMapper.getByUuNumber(receiverId);
                Map<String, Object> request = BuildRelaUtil.buildRequest(sender, receiver, relation);

                requestList.add(request);
                userMapper.requestSended(currentId, senderId);
            }

        }

        return requestList;
    }

    @Override
    public List<FriendInfoVO> getAllFriends() {
        long currentId = BaseContext.getCurrentId();
        List<FriendInfoVO> friendList = new ArrayList<>();

        List<FriendRelationship> mFriendList = userMapper.getAllFriends(currentId, FriendStatuType.ACCEPTED.getValue());
        for (FriendRelationship friend : mFriendList) {
            long friendId = friend.getSenderId() == currentId ? friend.getReceiverId() : friend.getSenderId();
            User friendInfo = userMapper.getByUuNumber(friendId);
            log.info("===================好友信息={}", friendInfo);
            FriendInfoVO friendInfoVO = BuildFriendInfoUtil.buildFriendInfo(friendInfo);
            friendList.add(friendInfoVO);
        }

        return friendList;
    }

    @Override
    public List<MsgVO> handleMsg(MsgDTO msgDTO) {//发消息
        long currentId = BaseContext.getCurrentId();
        long targetUuNumber = msgDTO.getTargetUuNumber();//接收者
        String msg = msgDTO.getMsg();
        User receiver = userMapper.getByUuNumber(targetUuNumber);//接收者
        User sender = userMapper.getByUuNumber(currentId);//发送者
        log.info("handleMsg sender {}", sender);
        log.info("handleMsg receiver {}", receiver);
        Msg saveMsg = BuildMsg.buildSaveMsg(sender, receiver, msg);
        userMapper.saveHistoryMsg(saveMsg);
        log.info("msgid是{}", saveMsg.getId());

        Msg pushMsg = userMapper.getMsg(saveMsg.getId());
        List<Msg> msgList = new ArrayList<>();
        msgList.add(pushMsg);

        List<MsgVO> msgVOList = msgList.stream().map(it -> {
            MsgVO vo = new MsgVO();
            vo.setMsgId(it.getId());
            vo.setSenderId(it.getSenderId());
            vo.setReceiverId(it.getChatId());
            vo.setContent(it.getContent());
            vo.setType(it.getChatType());
            vo.setCreateAt(TimeUtil.dateTimeToSecond(it.getCreateAt()));
            return vo;
        }).collect(Collectors.toList());
        if (WebSocketServer.isUserOnline(receiver.getUuNumber())) {
            messagePushService.pushToUser(receiver.getUuNumber(), EventType.EVENT_TYPE_MSG, msgVOList);
        }

        return msgVOList;
    }

    @Override
    public List<MovieVO> getMovies() {
        List<Movie> movies = userMapper.getAllMovies();
        return movies.stream().map(Movie::toVO).collect(Collectors.toList());
    }

    @Override
    public List<RoomVO> getRooms() {
        List<Room> roomList = userMapper.getAllRooms();
        List<RoomVO> roomVOList = new ArrayList<>();

        for (Room room : roomList) {
            Movie movie = userMapper.getMovieById(room.getMovieId());
            String movieUrl = movie.getMovieUrl();
            String movieName = movie.getMovieName();
            String movieCover = movie.getMovieCover();

            RoomVO roomVO = new RoomVO();
            roomVO.setRoomId(room.getRoomId());
            roomVO.setMovieUrl(movieUrl);
            roomVO.setMovieCover(UrlUtil.fillUrl(movieCover));
            roomVO.setMovieName(movieName);
            roomVOList.add(roomVO);
        }
        return roomVOList;
    }

    @Override
    public void saveRoom(SaveRoomDTO roomDTO) {
        String roomId = roomDTO.getRoomId();
        int movieId = roomDTO.getMovieId();
        userMapper.saveRoom(roomId, movieId);
    }

    @Override
    public void removeRoom(RemoveRoomDTO removeRoomDTO) {
        String roomId = removeRoomDTO.getRoomId();
        userMapper.deleteRoom(roomId);
    }

    @Override
    public UserVO updateUserInfo(UpdateUserinfoDTO updateUserDTO) {
        long currentId = BaseContext.getCurrentId();
        UpdateUserType type = updateUserDTO.getType();
        switch (type) {
            case USERNAME:
                userMapper.updateUserInfo(currentId, (String) updateUserDTO.getData());
                break;
//            case EMAIL:
//                userMapper.updateEmail(currentId, updateUserDTO.getData());
//                break;
//            case GENDER:
//                userMapper.updateGender(currentId,updateUserDTO.getData());
//                break;
//            case SIGNATURE:
//                userMapper.updateSignature(currentId,updateUserDTO.getData());
//                break;
//            case PHONE:
//                userMapper.updatePhone(currentId, updateUserDTO.getData());
//                break;
            default:
                break;
        }

        User user = userMapper.getByUuNumber(currentId);
        Auth authById = userMapper.getAuthById(AuthType.EMAIL.getValue(), user.getUuNumber());
        Auth authByPhone = userMapper.getAuthById(AuthType.PHONE.getValue(), user.getUuNumber());
        return UserVO.builder()
                .username(user.getUsername())
                .avatarUrl(UrlUtil.fillUrl(user.getAvatarUrl()))
                .email(authById.getAuthValue())
                .phone(authByPhone.getAuthValue())
                .uuNumber(currentId)
                .createAt(TimeUtil.dateTimeToSecond(user.getCreateAt()))
                .updateAt(TimeUtil.dateTimeToSecond(user.getUpdateAt()))
                .build();
    }

    @Override
    public int saveShareMessage(ShareMessageDTO shareDTO) {
        long currentId = BaseContext.getCurrentId();
        User user = userMapper.getByUuNumber(currentId);

        int targetId = shareDTO.getTargetId();
        User targetUser = userMapper.getByUuNumber(targetId);

        Msg shareMsg = BuildMsg.buildShareMsg(user, targetUser, shareDTO);
        userMapper.saveHistoryMsg(shareMsg);

        boolean isOnline = WebSocketServer.isUserOnline(targetUser.getUuNumber());
        if (isOnline) {
            Map<String, Object> shareContent = new HashMap<>();
            shareContent.put("senderId", user.getUuNumber());
            shareContent.put("receiverId", shareDTO.getTargetId());
            shareContent.put("linkTitle", shareDTO.getLinkTitle());
            shareContent.put("linkContent", shareDTO.getLinkContent());
            shareContent.put("linkImageUrl", shareDTO.getLinkImageUrl());

            List<Map<String, Object>> msgList = new ArrayList<>();
            msgList.add(shareContent);

            messagePushService.pushToUser(targetUser.getUuNumber(), EventType.EVENT_TYPE_SHAREMSG, msgList);
        }
        return 0;
    }

    @Override
    public PostsVO publishPost(PostDTO postsDTO) {
        List<MultipartFile> imageList = postsDTO.getImages();

        long currentId = BaseContext.getCurrentId();
        Post needSavePost = new Post();
        log.info("publishPost postsDTO {}", postsDTO);
        needSavePost.setUserId(currentId);
        needSavePost.setTitle(postsDTO.getTitle());
        needSavePost.setContent(postsDTO.getContent());
        needSavePost.setLikeCount(0);

        userMapper.savePost(needSavePost);
        Post savedPost = userMapper.getPostById(needSavePost.getId());

        //保存图片
        List<String> postImageUrlList = new ArrayList<>();
        if (imageList != null && !imageList.isEmpty()) {
            String uploadDir = "C:/postImages/";
            String urlPrefix = "/postImages/";

            for (int i = 0; i < imageList.size(); i++) {
                MultipartFile file = imageList.get(i);
                try {

                    String imageUrl = saveFileToDisk(file, uploadDir, urlPrefix);
                    postImageUrlList.add(imageUrl);
                    PostImages postImages = new PostImages();
                    postImages.setImageUrl(imageUrl);
                    postImages.setPostId(savedPost.getId());
                    postImages.setSerialNum(i + 1);
                    userMapper.savePostImages(postImages);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }
        String coverUrl = null;
        if (!postImageUrlList.isEmpty()) {
            coverUrl = postImageUrlList.get(0);
        }

        return PostsVO.builder()
                .postId(savedPost.getId())
                .userId(currentId)
                .content(savedPost.getContent())
                .imageUrls(postImageUrlList)
                .coverUrl(coverUrl)
                .createAt(TimeUtil.dateTimeToSecond(savedPost.getCreateAt()))
                .build();
    }

//    @Override
//    public PostsVO publishPost(OssPostsDTO postsDTO) {
//        long currentId = BaseContext.getCurrentId();
//        List<String> imageList = postsDTO.getImages();
//
//        Post needSavePost = new Post();
//        needSavePost.setUserId(currentId);
//        needSavePost.setTitle(postsDTO.getTitle());
//        needSavePost.setContent(postsDTO.getContent());
//        needSavePost.setLikeCount(0);
//
//        userMapper.savePost(needSavePost);
//        Post savedPost = userMapper.getPostById(needSavePost.getId());
//
//        List<String> postImageUrlList = new ArrayList<>();
//        if (imageList == null || imageList.isEmpty()) {
//            log.info("[x] publishPost #483");
//            return null;
//        }
//
//        for (int i = 0; i < imageList.size(); i++) {
//            String imageUrl = imageList.get(i);
//            postImageUrlList.add(imageUrl);
//            PostImages postImages = new PostImages();
//            postImages.setImageUrl(imageUrl);
//            postImages.setPostId(savedPost.getId());
//            postImages.setSerialNum(i + 1);
//            userMapper.savePostImages(postImages);
//        }
//
//        String coverUrl;
//        if (postImageUrlList.isEmpty()) {
//            log.info("[x] publishPost #499");
//            return null;
//        }
//
//        coverUrl = postImageUrlList.get(0);
//        return PostsVO.builder()
//                .postId(savedPost.getId())
//                .userId(currentId)
//                .title(savedPost.getTitle())
//                .content(savedPost.getContent())
//                .imageUrls(postImageUrlList)
//                .coverUrl(coverUrl)
//                .createAt(TimeUtil.dateTimeToSecond(savedPost.getCreateAt()))
//                .build();
//    }

    @Override
    public PageResult<PostsVO> getPosts(PostsQueryDTO postsQueryDTO) {
        int page = Math.max(postsQueryDTO.getPage(), 1);
        int size = Math.max(postsQueryDTO.getSize(), 1);
        int offset = (page - 1) * size;

        List<Post> postList = userMapper.getPosts(offset, size + 1);
        return getPostsVOPageResult(page, size, postList);
    }

    @Override
    public PageResult<PostsVO> getMyPosts(PostsQueryDTO postsQueryDTO) {
        long currentId = BaseContext.getCurrentId();
        int page = Math.max(postsQueryDTO.getPage(), 1);
        int size = Math.max(postsQueryDTO.getSize(), 1);
        int offset = (page - 1) * size;
        List<Post> postList = userMapper.getMyPosts(currentId, offset, size + 1);
        return getPostsVOPageResult(page, size, postList);
    }

    @Override
    public PageResult<PostsVO> getFollowerPost(PostsQueryDTO postsQueryDTO) {
        long currentId = BaseContext.getCurrentId();
        int page = Math.max(postsQueryDTO.getPage(), 1);
        int size = Math.max(postsQueryDTO.getSize(), 1);
        int offset = (page - 1) * size;

        List<Long> followedUserIds = userMapper.getFollowedUserIds(currentId);
        if (followedUserIds == null || followedUserIds.isEmpty()) {
            return PageResult.<PostsVO>builder()
                    .resultList(Collections.emptyList())
                    .page(page)
                    .size(size)
                    .hasNext(false)
                    .build();
        }

        List<Post> postList = userMapper.getPostsByUserIds(followedUserIds, offset, size + 1);
        return getPostsVOPageResult(page, size, postList);
    }

    @Override
    public StsVO getSts() {

        return StsVO.builder().build();
    }

    @Override
    public PageResult<MsgVO> getMsg(GetMsgDTO getMsgDTO) {
        long currentId = BaseContext.getCurrentId();
        int page = Math.max(getMsgDTO.getPage(), 1);
        int size = Math.max(getMsgDTO.getSize(), 1);
        int offset = (page - 1) * size;

        if (getMsgDTO.getChatType() == 0) {


        } else if (getMsgDTO.getChatType() == 1) {

        } else {
            log.info("[x] getMsg #");
        }

        List<Msg> pageMsg = userMapper.getUserMsg(currentId, getMsgDTO.getChatId(), offset, size + 1);
        if (pageMsg == null || pageMsg.isEmpty()) {
            return PageResult.<MsgVO>builder()
                    .resultList(Collections.emptyList())
                    .page(page)
                    .size(size)
                    .hasNext(false)
                    .build();
        }
        User user = userMapper.getByUuNumber(currentId);
        User chatUser = userMapper.getByUuNumber(getMsgDTO.getChatId());

        List<MsgVO> msgVOList = pageMsg.stream().map(msg -> {
            MsgVO vo = new MsgVO();
            vo.setMsgId(msg.getId());
            vo.setSenderId(msg.getSenderId());
            vo.setReceiverId(msg.getChatId());
            vo.setContent(msg.getContent());
            vo.setType(msg.getChatType());
            vo.setCreateAt(TimeUtil.dateTimeToSecond(msg.getCreateAt()));
            return vo;
        }).collect(Collectors.toList());


        return PageResult.<MsgVO>builder()
                .resultList(msgVOList)
                .page(page)
                .size(size)
                .hasNext(false)
                .build();
    }

    private PageResult<PostsVO> getPostsVOPageResult(int page, int size, List<Post> postList) {
        if (postList == null || postList.isEmpty()) {
            log.info("[x] getPostsVOPageResult #570");
            return PageResult.<PostsVO>builder()
                    .resultList(Collections.emptyList())
                    .page(page)
                    .size(size)
                    .hasNext(false)
                    .build();
        }

        boolean hasNext = postList.size() > size;
        if (hasNext) {
            postList = postList.subList(0, size);
        }

        List<Integer> postIds = postList.stream()
                .map(Post::getId)
                .collect(Collectors.toList());

        List<PostImages> imageList = userMapper.getImagesByPostIds(postIds);
        List<Integer> likedPostIds = userMapper.getLikedPostIdsByUser(postIds, BaseContext.getCurrentId());
        Set<Integer> likedPostIdSet = likedPostIds == null
                ? Collections.emptySet()
                : new HashSet<>(likedPostIds);

        List<PostsVO> postsVOList = null;
        if (imageList != null && !imageList.isEmpty()) {
            Map<Integer, List<PostImages>> imageMap = imageList.stream().collect(Collectors.groupingBy(PostImages::getPostId));
            postsVOList = postList.stream()
                    .map(post -> convertToPostsVO(post, imageMap, likedPostIdSet))
                    .collect(Collectors.toList());
        }

        return PageResult.<PostsVO>builder()
                .resultList(postsVOList)
                .page(page)
                .size(size)
                .hasNext(hasNext)
                .build();
    }

    private PostsVO convertToPostsVO(Post post, Map<Integer, List<PostImages>> imageMap, Set<Integer> likedPostIdSet) {
        User user = userMapper.getByUuNumber(post.getUserId());
        PostsVO.PostsVOBuilder postsVOBuilder = PostsVO.builder()
                .postId(post.getId())
                .userId(post.getUserId())
                .nickName(user.getUsername())
                .userAvatarUrl(UrlUtil.fillUrl(user.getAvatarUrl()))
                .title(post.getTitle())
                .content(post.getContent())
                .likeCount(post.getLikeCount())
                .isLiked(likedPostIdSet.contains(post.getId()) ? 1 : 0)
                .createAt(TimeUtil.dateTimeToSecond(post.getCreateAt()));

        List<String> imageUrlsList = Optional.ofNullable(imageMap.get(post.getId()))
                .orElse(Collections.emptyList())
                .stream()
                .sorted(Comparator.comparingInt(PostImages::getSerialNum))
                .map(PostImages::getImageUrl)
                .map(UrlUtil::fillUrl)
                .collect(Collectors.toList());

        String coverUrl = null;
        if (!imageUrlsList.isEmpty()) {
            coverUrl = imageUrlsList.get(0);
        }

        postsVOBuilder.coverUrl(coverUrl);
        postsVOBuilder.imageUrls(imageUrlsList);
        log.info("postsVOBuilder: {}", postsVOBuilder);
        return postsVOBuilder.build();
    }


    @Override
    public PageResult<GetCommentVO> getComment(GetCommentDTO commentDTO) {
        int postId = commentDTO.getPostId();
        int page = commentDTO.getPage();
        int size = commentDTO.getSize();
        int offset = (page - 1) * size;

        // 1. 获取父评论分页
        List<Comment> parentComments = userMapper.getCommentByPostId(postId, size + 1, offset);
        boolean hasNext = parentComments.size() > size;
        if (hasNext) parentComments = parentComments.subList(0, size);

        if (parentComments.isEmpty()) {
            return PageResult.<GetCommentVO>builder()
                    .resultList(Collections.emptyList())
                    .page(page)
                    .size(size)
                    .hasNext(false)
                    .build();
        }

        // 2. 转换父评论为 VO
        List<GetCommentVO> parentVOList = parentComments.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 3. 递归填充子评论
        fillChildComments(parentVOList);

        return PageResult.<GetCommentVO>builder()
                .resultList(parentVOList)
                .page(page)
                .size(size)
                .hasNext(hasNext)
                .build();
    }

    private void fillChildComments(List<GetCommentVO> parentVOs) {
        List<Integer> parentIds = parentVOs.stream()
                .map(GetCommentVO::getId)
                .collect(Collectors.toList());

        if (parentIds.isEmpty()) return;

        // 批量获取直接子评论
        List<Comment> childComments = userMapper.getChildComments(parentIds);
        if (childComments.isEmpty()) return;

        // 转换为 VO
        List<GetCommentVO> childVOs = childComments.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 按 parentId 分组
        Map<Integer, List<GetCommentVO>> childMap = childVOs.stream()
                .collect(Collectors.groupingBy(GetCommentVO::getParentId));

        // 填充到父评论
        for (GetCommentVO parent : parentVOs) {
            List<GetCommentVO> children = childMap.getOrDefault(parent.getId(), Collections.emptyList());
            parent.setChildCommentList(children);
            // 递归填充子评论的子评论
            fillChildComments(children);
        }
    }


    private GetCommentVO convertToVO(Comment comment) {
        GetCommentVO vo = new GetCommentVO();
        vo.setId(comment.getId());
        vo.setPostId(comment.getPostId());
        vo.setUserId(comment.getUserId());
        // todo 提前批量查出
        String username = userMapper.getByUuNumber(comment.getUserId()).getUsername();
        vo.setUserName(username);
        if (comment.getReplyToUserId() > 0) {
            User replyToUser = userMapper.getByUuNumber(comment.getReplyToUserId());
            vo.setReplyToUserName(replyToUser.getUsername());

        }

        vo.setParentId(comment.getParentId());
        vo.setReplyToUserId(comment.getReplyToUserId());
        vo.setContent(comment.getContent());
        vo.setCreateAt(TimeUtil.dateTimeToSecond(comment.getCreateAt()));
        return vo;
    }

    @Override
    public Result<Comment> addComment(AddCommentDTO addCommentDTO) {
        long currentId = BaseContext.getCurrentId();
        Comment comment = new Comment();
        comment.setPostId(addCommentDTO.getPostId());
        comment.setUserId(currentId);
        if (addCommentDTO.getParentId() == -1) {
            comment.setParentId(-1);
        } else {
            comment.setParentId(addCommentDTO.getParentId());
            comment.setReplyToUserId(addCommentDTO.getReplyToUserId());
        }
        comment.setContent(addCommentDTO.getContent());
        userMapper.addComment(comment);
        return Result.success(comment);
    }


    @Override
    public FollowUserVO followUser(FollowUserDTO dto) {
        long currentId = BaseContext.getCurrentId();
        userMapper.followUser(currentId, dto.getUserId());
        return null;
    }

    @Override
    public UnFollowUserVO unFollowUser(FollowUserDTO dto) {
        long currentId = BaseContext.getCurrentId();
        userMapper.cancelFollowUser(currentId, dto.getUserId());
        return null;
    }

    @Override
    public List<MovieCateVO> getMovieCategory() {
        List<MovieCategory> movieCategory = userMapper.getMovieCategory();

        return movieCategory.stream()
                .map(category -> new MovieCateVO(
                        category.getCate_id(),
                        category.getCate_name()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public void saveMovieProgress(SaveProgressDTO dto) {
        long userId = BaseContext.getCurrentId();

        // 查找是否存在该用户和该电影的记录
        MovieHistory existingHistory = userMapper.findByUserIdAndMovieId(userId, dto.getMovieId());

        if (existingHistory != null) {
            // 如果记录已存在，更新进度
            existingHistory.setProgress(dto.getCurrentProgress());
            if (existingHistory.getProgress() >= 95) {
                existingHistory.setWatchCount(existingHistory.getWatchCount() + 1);
            }

            userMapper.updateMovieProgress(existingHistory);
        } else {
            // 如果没有记录，插入新的记录
            MovieHistory newHistory = new MovieHistory();
            newHistory.setMovieId(dto.getMovieId());
            newHistory.setUserId(userId);
            newHistory.setWatchCount(1);
            newHistory.setProgress(dto.getCurrentProgress());

            userMapper.insertMovieProgress(newHistory);
        }
    }

    @Override
    public List<MovieHistoryVO> getWatchHistory() {
        long userId = BaseContext.getCurrentId();
        List<MovieHistory> movieHistoryList = userMapper.getWatchHistory(userId);
        return movieHistoryList.stream()
                .map(movieHistory -> new MovieHistoryVO(
                        movieHistory.getMovieId(),
                        movieHistory.getUserId(),
                        movieHistory.getWatchCount(),
                        movieHistory.getProgress(),
                        movieHistory.getCreateAt(),
                        movieHistory.getUpdateAt()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public ActorProfileVO getActorProfile(ActorProfileDTO dto) {
        Actor actor = userMapper.getActorProfile(dto.getActorId());
        if (actor == null) {
            log.info("[x] getActorProfile #1031");
            return null;
        }

        return new ActorProfileVO(
                actor.getActorId(),
                actor.getActorName(),
                actor.getActorIntro(),
                actor.getActorGender(),
                actor.getActorAvatar()
        );
    }

    @Override
    public List<PostsVO> getLikePost() {
        long userId = BaseContext.getCurrentId();
        List<PostLike> likePost = userMapper.getLikePost(userId);
        if (likePost == null || likePost.isEmpty()) {
            log.info("[x] 用户没有点赞任何帖子 #968");
            return Collections.emptyList();
        }

        return likePost.stream()
                .map(postLike -> {
                    Post post = userMapper.getPostById(postLike.getPostId());
                    if (post != null) {
                        return convertToPostsVO(post, null, null);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostsVO> getFootprintPost() {
        long userId = BaseContext.getCurrentId();
        List<FootPrint> footprintPost = userMapper.getFootprintPost(userId);
        if (footprintPost == null || footprintPost.isEmpty()) {
            log.info("[x] 用户没有浏览任何帖子 #989");
            return Collections.emptyList();
        }

        return footprintPost.stream()
                .map(footprint -> {
                    Post post = userMapper.getPostById(footprint.getPostId());
                    if (post != null) {
                        return convertToPostsVO(post, null, null);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Boolean likePostIfNeed(LikePostDTO dto) {
        long userId = BaseContext.getCurrentId();
        PostLike postLike = userMapper.selectExistLikePost(userId, dto.getPostId());
        if (postLike == null) {
            userMapper.insertLikePost(userId, dto.getPostId());
        } else {
            userMapper.deleteLikePost(userId, dto.getPostId());
        }
        return true;
    }
}
