package com.example.test.server.service.implClass;


import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.example.test.common.constant.MessageConstant;
import com.example.test.common.context.BaseContext;
import com.example.test.common.enums.EventType;
import com.example.test.common.exception.*;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            throw new SendException("未知错误,验证码发送失败");
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


        User isExist = userMapper.getByEmail(email);
        if (isExist != null) {
            throw new DuplicateEmailException(MessageConstant.ALREADY_REGISTED);
        }

        //TODO 复杂密码验证


        User user = new User();
        try {

            // 生成唯一UUNumber（YYMMDDHHmm+3位毫秒）
            long timestamp = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
            String timePart = sdf.format(new Date(timestamp));
            String millisPart = String.format("%03d", timestamp % 1000);
            long uuNumber = Long.parseLong(timePart + millisPart);

            user.setEmail(email);
            user.setPassword(DigestUtils.md5DigestAsHex(registerDTO.getPassword().getBytes()));
            user.setUuNumber(uuNumber);
            user.setUsername("WQ_" + uuNumber);
            user.setAvatarUrl("");
            user.setVersion(1);

            userMapper.insert(user);

            verifyUtil.deleteCode(email);

        } catch (Exception e) {
            throw new BaseException(MessageConstant.UNKNOWN_ERROR);
        }
        return user;
    }


    @Override
    public User login(UserLoginDTO userLoginDTO) {
        String email = userLoginDTO.getEmail();
        String password = userLoginDTO.getPassword();

        User user = userMapper.getByEmail(email);
        if (user == null) {
            throw new UserNotFoundException(MessageConstant.PASSWORD_ERROR);
        }

        password = DigestUtils.md5DigestAsHex(password.getBytes());

        log.info("Md5加密密码:{}", password);
        if (!password.equals(user.getPassword())) {
            throw new InvalidPasswordException(MessageConstant.PASSWORD_ERROR);
        }

        //TODO
//        if (employee.getStatus() == StatusConstant.DISABLE) {
//            //账号被锁定
//            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
//        }


        return user;
    }


    @Override
    public User autoLogin(Object obj) {
        User user = null;
        if (obj instanceof String) {
            String email = (String) obj;
            user = loginByEmail(email);
        } else if (obj instanceof Long) {
            int phoneNum = (int) obj;
            //TODO
//            user = loginByPhone(phoneNum);
        }

        return user;
    }

    private User loginByEmail(String email) {
        User user = userMapper.getByEmail(email);
        if (user == null) {
            throw new UserNotFoundException(MessageConstant.PASSWORD_ERROR);
        }

        return user;
    }

    @Override
    public String updateAvatar(AvatarUploadDTO avatarUploadDTO) {


        MultipartFile file = avatarUploadDTO.getFile();

        if (file == null || file.isEmpty()) {
            throw new InvalidAvatarException("上传文件不能为空");
        }

        String contentType = avatarUploadDTO.getFile().getContentType();
        if (!Arrays.asList("image/jpeg", "image/png").contains(contentType)) {
            throw new InvalidAvatarException("仅支持JPG/PNG格式");
        }

        try {

            long currentId = BaseContext.getCurrentId();


            User oldUser = userMapper.getByUuNumber(currentId); // 获取旧用户信息
            String oldAvatarUrl = oldUser.getAvatarUrl();

            String uploadDir = "C:/avatar/";
            String urlPrefix = "/avatar/";
            String avatarUrl = "";
            //TODO 更新数据库中用户头像的路径


            log.info("currentId:{}", currentId);
            userMapper.updateAvatar(currentId, avatarUrl);

            if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
                deleteOldAvatarFile(oldAvatarUrl);
            }

            return avatarUrl;
        } catch (Exception e) {
            log.info("[x] 头像更新失败MessageConstant.UNKNOWN_ERROR #220");
            throw new BaseException(MessageConstant.UNKNOWN_ERROR + "头像更新失败");
        }
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
    public User searchUser(SearchUserDTO searchUserDTO) {
        String phone = searchUserDTO.getPhone();
        long uuNumber = searchUserDTO.getUuNumber();
        String email = searchUserDTO.getEmail();
        User mUser = new User();
        if (!phone.isEmpty())
            mUser = userMapper.getByPhone(phone);
        if (uuNumber > 0)
            mUser = userMapper.getByUuNumber(uuNumber);
        if (!email.isEmpty())
            mUser = userMapper.getByEmail(email);

        if (mUser == null) {
            throw new UserNotFoundException("用户不存在");
        }
        User user = new User();
        user.setUsername(mUser.getUsername());
        user.setAvatarUrl(mUser.getAvatarUrl());
        user.setEmail(mUser.getEmail());
        user.setUuNumber(mUser.getUuNumber());

        return user;
    }

    @Override
    public String FriendApply(FriendApplyDTO friendApplyDTO) {
        long targetId = friendApplyDTO.getTargetId();
        long currentId = BaseContext.getCurrentId();
        User receiver = userMapper.getByUuNumber(targetId);

        if (userMapper.existsPendingApply(currentId, targetId)) {
            throw new BaseException("请勿重复申请");
        }

        User sender = userMapper.getByUuNumber(currentId);

        FriendRelationship relationship = new FriendRelationship();
        relationship.setStatus("pending");
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
        } else {
            userMapper.saveOfflineMsg(relationship.getId(), EventType.getIntEventType(EventType.EVENT_TYPE_REQUEST_FRIEND), currentId, targetId);
        }

        return "sended";
    }

    @Override
    public HandleFriendRequestVO handleResponse(HandleFriendRequestDTO friendRequestDTO) {
        long sourceUuNumber = friendRequestDTO.getSourceUuNumber();//申请者
        boolean isAgree = friendRequestDTO.isAgree();
        log.info("sourceUuNumber {}", sourceUuNumber);
        log.info("isAgree {}", isAgree);
        User requester = userMapper.getByUuNumber(sourceUuNumber);
        long currentId = BaseContext.getCurrentId();

        if (isAgree) {
            log.info("isAgree enter");
            userMapper.updateStateAgree(requester.getUuNumber(), currentId);
            userMapper.insertFriend(requester.getUuNumber(), BaseContext.getCurrentId());
        } else {
            log.info("! isAgree ");
            userMapper.updateStateReject(requester.getUuNumber(), currentId);
        }
        HandleFriendRequestVO vo = new HandleFriendRequestVO();
        FriendRelationship targetRela = userMapper.getTargetRela(sourceUuNumber,currentId);
        vo.setFriendRelationship(targetRela);

        if (isAgree) {
            UserVO userVO = new UserVO();
            userVO.setUsername(requester.getUsername());
            userVO.setAvatarUrl(requester.getAvatarUrl());
            userVO.setEmail(requester.getEmail());
            userVO.setUuNumber(requester.getUuNumber());
            userVO.setUpdateAt(-1L);
            vo.setUser(userVO);
        }

        return vo;
    }

    @Override
    public List<Map<String, Object>> getAllFriendRequest() {

        List<FriendRelationship> relationshipList = userMapper.getAllRela(BaseContext.getCurrentId());

        List<Map<String, Object>> requestList = new ArrayList<>();
        if (!relationshipList.isEmpty()) {

            for (FriendRelationship relation : relationshipList) {
                long senderId = relation.getSenderId();
                long receiverId = relation.getReceiverId();
                User sender = userMapper.getByUuNumber(senderId);
                User receiver = userMapper.getByUuNumber(receiverId);
                Map<String, Object> request = BuildRelaUtil.buildRequest(sender, receiver, relation);

                requestList.add(request);
                userMapper.requestSended(BaseContext.getCurrentId(), senderId);
            }

        }

        return requestList;
    }

    @Override
    public List<FriendInfoVO> getAllFriends() {
        List<FriendInfoVO> friendList = new ArrayList<>();
        long currentId = BaseContext.getCurrentId();

        List<Friend> mFriendList = userMapper.getAllFriends(currentId);
        for (Friend friend : mFriendList) {
            long friendId = friend.getUserUuid() == currentId ? friend.getFriendUuid() : friend.getUserUuid();
            User friendInfo = userMapper.getByUuNumber(friendId);
            log.info("===================好友信息={}", friendInfo);
            FriendInfoVO friendInfoVO = BuildFriendInfoUtil.buildFriendInfo(friendInfo);
            friendList.add(friendInfoVO);
        }

        return friendList;
    }

    @Override
    public List<MsgVO> handleMsg(MsgDTO msgDTO) {//发消息
        long targetUuNumber = msgDTO.getTargetUuNumber();//接收者
        String msg = msgDTO.getMsg();
        long senderId = BaseContext.getCurrentId();//当前用户id
        User receiver = userMapper.getByUuNumber(targetUuNumber);//接收者
        User sender = userMapper.getByUuNumber(senderId);//发送者
        log.info("handleMsg sender {}",sender);
        log.info("handleMsg receiver {}",receiver);
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
            vo.setReceiverId(it.getReceiverId());
            vo.setContent(it.getContent());
            vo.setType(it.getType());
            vo.setCreateAt(TimeUtil.dateTimeToSecond(it.getCreateAt()));
            return vo;
        }).collect(Collectors.toList());
        if (WebSocketServer.isUserOnline(receiver.getUuNumber())) {
            messagePushService.pushToUser(receiver.getUuNumber(), EventType.EVENT_TYPE_MSG, msgVOList);
        } else {
            userMapper.saveOfflineMsg(saveMsg.getId(), EventType.getIntEventType(EventType.EVENT_TYPE_MSG), senderId, receiver.getUuNumber());
        }

        return msgVOList;
    }

    @Override
    public List<Movie> getMovies() {
        return userMapper.getAllMovies();
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
            roomVO.setMovieCover(movieCover);
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
    public void updateUserInfo(UpdateUserInfoDTO updateUserInfoDTO) {
        long currentId = BaseContext.getCurrentId();
        userMapper.updateUserInfo(currentId, updateUserInfoDTO.getUserName());
    }

    @Override
    public int saveShareMessage(ShareMessageDTO shareDTO) {
        long currentId = BaseContext.getCurrentId();
        User user = userMapper.getByUuNumber(currentId);
        String targetEmail = shareDTO.getTargetEmail();
        User targetUser = userMapper.getByEmail(targetEmail);
        Msg shareMsg = BuildMsg.buildShareMsg(user, targetUser, shareDTO);
        userMapper.saveHistoryMsg(shareMsg);

        boolean isOnline = WebSocketServer.isUserOnline(targetUser.getUuNumber());
        if (isOnline) {
            Map<String, Object> shareContent = new HashMap<>();
            shareContent.put("senderEmail", user.getEmail());
            shareContent.put("receiverEmail", shareDTO.getTargetEmail());
            shareContent.put("linkTitle", shareDTO.getLinkTitle());
            shareContent.put("linkContent", shareDTO.getLinkContent());
            shareContent.put("linkImageUrl", shareDTO.getLinkImageUrl());

            List<Map<String, Object>> msgList = new ArrayList<>();
            msgList.add(shareContent);

            messagePushService.pushToUser(targetUser.getUuNumber(), EventType.EVENT_TYPE_SHAREMSG, msgList);
        } else {
            userMapper.saveOfflineMsg(shareMsg.getId(), EventType.getIntEventType(EventType.EVENT_TYPE_SHAREMSG), currentId, targetUser.getUuNumber());
        }
        return 0;
    }

    @Override
    public PostsVO publishPost(PostsDTO postsDTO) {
        List<String> imageList = postsDTO.getImages();

        Post needSavePost = new Post();
        long currentId = BaseContext.getCurrentId();
        needSavePost.setUserId(currentId);
        needSavePost.setTitle(postsDTO.getTitle());
        needSavePost.setContent(postsDTO.getContent());
        needSavePost.setLikeCount(0);

        userMapper.savePost(needSavePost);
        Post savedPost = userMapper.getPostById(needSavePost.getId());

        List<String> postImageUrlList = new ArrayList<>();
        if (imageList == null || imageList.isEmpty()) {
            log.info("[x] publishPost #483");
            return null;
        }

        for (int i = 0; i < imageList.size(); i++) {
            String imageUrl = imageList.get(i);
            postImageUrlList.add(imageUrl);
            PostImages postImages = new PostImages();
            postImages.setImageUrl(imageUrl);
            postImages.setPostId(savedPost.getId());
            postImages.setSerialNum(i + 1);
            userMapper.savePostImages(postImages);
        }

        String coverUrl;
        if (postImageUrlList.isEmpty()) {
            log.info("[x] publishPost #499");
            return null;
        }

        coverUrl = postImageUrlList.get(0);
        return PostsVO.builder()
                .postId(savedPost.getId())
                .userId(currentId)
                .title(savedPost.getTitle())
                .content(savedPost.getContent())
                .imageUrls(postImageUrlList)
                .coverUrl(coverUrl)
                .createAt(TimeUtil.dateTimeToSecond(savedPost.getCreateAt()))
                .build();
    }

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
        int page = Math.max(postsQueryDTO.getPage(), 1);
        int size = Math.max(postsQueryDTO.getSize(), 1);
        int offset = (page - 1) * size;
        long currentId = BaseContext.getCurrentId();
        List<Post> postList = userMapper.getMyPosts(currentId, offset, size + 1);
        return getPostsVOPageResult(page, size, postList);
    }

    @Override
    public StsVO getSts() {

        return StsVO.builder().build();
    }

    @Override
    public PageResult<MsgVO> getMsg(GetMsgDTO getMsgDTO) {
        int page = Math.max(getMsgDTO.getPage(), 1);
        int size = Math.max(getMsgDTO.getSize(), 1);
        int offset = (page - 1) * size;

        long currentId = BaseContext.getCurrentId();
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
            vo.setReceiverId(msg.getReceiverId());
            vo.setContent(msg.getContent());
            vo.setType(msg.getType());
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
        List<PostsVO> postsVOList = null;
        if (imageList != null && !imageList.isEmpty()) {
            Map<Integer, List<PostImages>> imageMap = imageList.stream().collect(Collectors.groupingBy(PostImages::getPostId));
            postsVOList = postList.stream()
                    .map(post -> convertToPostsVO(post, imageMap))
                    .collect(Collectors.toList());
        }

        return PageResult.<PostsVO>builder()
                .resultList(postsVOList)
                .page(page)
                .size(size)
                .hasNext(hasNext)
                .build();
    }

    private PostsVO convertToPostsVO(Post post, Map<Integer, List<PostImages>> imageMap) {
        User user = userMapper.getByUuNumber(post.getUserId());
        PostsVO.PostsVOBuilder postsVOBuilder = PostsVO.builder()
                .postId(post.getId())
                .userId(post.getUserId())
                .nickName(user.getUsername())
                .userAvatarUrl(UrlUtil.fillUrl(user.getAvatarUrl()))
                .content(post.getContent())
                .likeCount(post.getLikeCount())
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
        vo.setCreateAt(comment.getCreateAt().getTime() / 1000);
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


}
