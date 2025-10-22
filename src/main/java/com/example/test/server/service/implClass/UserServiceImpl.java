package com.example.test.server.service.implClass;


import com.example.test.common.constant.MessageConstant;
import com.example.test.common.context.BaseContext;
import com.example.test.common.exception.*;
import com.example.test.common.result.PageResult;
import com.example.test.common.result.Result;
import com.example.test.common.utils.BuildFriendInfoUtil;
import com.example.test.common.utils.BuildMsg;
import com.example.test.common.utils.BuildRelaUtil;
import com.example.test.common.utils.VerifyUtil;
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
import java.math.BigInteger;
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
            BigInteger uuNumber = new BigInteger(timePart + millisPart);

            user.setEmail(email);
            user.setPassword(DigestUtils.md5DigestAsHex(registerDTO.getPassword().getBytes()));
            user.setUuid(UUID.randomUUID().toString());
            user.setUuNumber(uuNumber);
            user.setUsername(uuNumber.toString());
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

    private User loginByEmail(String email){
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

            String currentId = BaseContext.getCurrentId();


            User oldUser = userMapper.getById(currentId); // 获取旧用户信息
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

            return avatarUrl;
        } catch (Exception e) {
            e.printStackTrace();
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


    @Override
    public User searchUser(SearchUserDTO searchUserDTO) {
        String phone = searchUserDTO.getPhone();
        String uuNumber = searchUserDTO.getUuNumber();
        String email = searchUserDTO.getEmail();
        User mUser = new User();
        if (!phone.isEmpty())
            mUser = userMapper.getByPhone(phone);
        if (!uuNumber.isEmpty())
            mUser = userMapper.getByUuNumber(BigInteger.valueOf(Long.parseLong(uuNumber)));
        if (!email.isEmpty())
            mUser = userMapper.getByEmail(email);

        if (mUser == null) {
            throw new UserNotFoundException("用户不存在");
        }
        User user = new User();
        user.setUsername(mUser.getUsername());
        user.setAvatarUrl(mUser.getAvatarUrl());
        user.setEmail(mUser.getEmail());
//        user.setPhone(phone);

        return user;
    }

    @Override
    public String FriendApply(FriendApplyDTO friendApplyDTO) {
        String targetEmail = friendApplyDTO.getTargetEmail();
        String targetId = userMapper.getByEmail(targetEmail).getUuid();
        String currentId = BaseContext.getCurrentId();

        if (userMapper.existsPendingApply(currentId, targetId)) {
            throw new BaseException("请勿重复申请");
        }
        userMapper.requestFriend(currentId, friendApplyDTO.getValidMsg(), targetId);
        List<FriendRelationship> relation = userMapper.getTargetRela(currentId, targetId);

        // 构造推送消息

        List<Map<String, Object>> requestList = new ArrayList<>();
        User sender = userMapper.getById(currentId);
        User receiver = userMapper.getById(targetId);
        Map<String, Object> request = BuildRelaUtil.buildRequest(sender, receiver, relation.get(0));

        requestList.add(request);
        Map<String, Object> message = new HashMap<>();
        message.put("event_type", "FRIEND_REQUEST");
        message.put("request_list", requestList);
        messagePushService.pushToUser(targetId, message);
        return "sended";
    }

    @Override
    public HandleFriendRequestVO handleResponse(HandleFriendRequestDTO friendRequestDTO) {
        String requestEmail = friendRequestDTO.getRequestEmail();//申请者
        User requester = userMapper.getByEmail(requestEmail);
        boolean isAgree = friendRequestDTO.isAgree();
        if (isAgree) {
            userMapper.updateStateAgree(requester.getUuid(), BaseContext.getCurrentId());
            userMapper.addFriend(requester.getUuid(), BaseContext.getCurrentId());
            List<FriendRelationship> targetRela = userMapper.getTargetRela(BaseContext.getCurrentId(), requester.getUuid());
            String validMsg = targetRela.get(0).getValidMsg();
            Msg msg = BuildMsg.setOriginMsg(requester.getUuid(), BaseContext.getCurrentId(), validMsg);
            userMapper.saveHistoryMsg(msg);
        } else {
            userMapper.updateStateReject(requester.getUuid(), BaseContext.getCurrentId());
        }
        HandleFriendRequestVO vo = new HandleFriendRequestVO();
        vo.setStatus(isAgree ? "accepted" : "rejected");
        vo.setUsername(requester.getUsername());
        vo.setAvatarUrl(requester.getAvatarUrl());
        vo.setEmail(requestEmail);

        return vo;
    }

    @Override
    public List<Map<String, Object>> getAllFriendRequest() {

        List<FriendRelationship> relationshipList = userMapper.getAllRela(BaseContext.getCurrentId());

        List<Map<String, Object>> requestList = new ArrayList<>();
        if (!relationshipList.isEmpty()) {

            for (FriendRelationship relation : relationshipList) {
                String senderId = relation.getSenderId();
                String receiverId = relation.getReceiverId();
                User sender = userMapper.getById(senderId);
                User receiver = userMapper.getById(receiverId);
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
        String currentId = BaseContext.getCurrentId();

        List<Friend> mFriendList = userMapper.getAllFriends(currentId);
        for (Friend friend : mFriendList) {
            String friendId = friend.getUserUuid().equals(currentId) ? friend.getFriendUuid() : friend.getUserUuid();
            User friendInfo = userMapper.getById(friendId);
            log.info("===================好友信息={}", friendInfo);
            FriendInfoVO friendInfoVO = BuildFriendInfoUtil.buildFriendInfo(friendInfo);
            friendList.add(friendInfoVO);
        }

        return friendList;
    }

    @Override
    public void handleMsg(MsgDTO msgDTO) {//发消息
        String targetEmail = msgDTO.getTargetEmail();//接收者
        String msg = msgDTO.getMsg();
        String senderId = BaseContext.getCurrentId();//当前用户id
        User receiver = userMapper.getByEmail(targetEmail);//接收者
        User sender = userMapper.getById(senderId);//发送者
        Msg saveMsg = BuildMsg.buildSaveMsg(sender, receiver, msg);
        userMapper.saveHistoryMsg(saveMsg);
        log.info("msgid是{}", saveMsg.getId());


        Msg pushMsg = BuildMsg.buildPushMsg(sender, receiver, msg);
        boolean isOnline = WebSocketServer.isUserOnline(receiver.getUuid());
        if (isOnline) {
            List<Msg> msgList = new ArrayList<>();

            msgList.add(pushMsg);

            Map<String, Object> message = new HashMap<>();
            message.put("event_type", "MESSAGE");
            message.put("msg_list", msgList);
            messagePushService.pushToUser(receiver.getUuid(), message);
        } else {
            userMapper.saveOfflineMsg(saveMsg.getId(), senderId, receiver.getUuid());
        }

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
        String currentId = BaseContext.getCurrentId();
        userMapper.updateUserInfo(currentId, updateUserInfoDTO.getUserName());
    }

    @Override
    public int saveShareMessage(ShareMessageDTO shareDTO) {
        String currentId = BaseContext.getCurrentId();
        User user = userMapper.getById(currentId);
        String targetEmail = shareDTO.getTargetEmail();
        User targetUser = userMapper.getByEmail(targetEmail);
        Msg shareMsg = BuildMsg.buildShareMsg(user, targetUser, shareDTO);
        userMapper.saveHistoryMsg(shareMsg);

        boolean isOnline = WebSocketServer.isUserOnline(targetUser.getUuid());
        if (isOnline) {
            Map<String, Object> shareContent = new HashMap<>();


            shareContent.put("senderEmail", user.getEmail());
            shareContent.put("receiverEmail", shareDTO.getTargetEmail());
            shareContent.put("linkTitle", shareDTO.getLinkTitle());
            shareContent.put("linkContent", shareDTO.getLinkContent());
            shareContent.put("linkImageUrl", shareDTO.getLinkImageUrl());


            List<Map<String, Object>> msgList = new ArrayList<>();
            msgList.add(shareContent);

            Map<String, Object> message = new HashMap<>();
            message.put("event_type", "SHARE");
            message.put("share_list", msgList);
            messagePushService.pushToUser(targetUser.getUuid(), message);
        } else {
            userMapper.saveOfflineMsg(shareMsg.getId(), currentId, targetUser.getUuid());
        }
        return 0;
    }

    @Override
    public PostsVO publishPost(PostsDTO postsDTO) {
        List<MultipartFile> imageList = postsDTO.getImages();

        Post post = new Post();
        String currentId = BaseContext.getCurrentId();
        post.setUserId(currentId);
        post.setContent(postsDTO.getContent());
        post.setLikeCount(0);

        userMapper.savePost(post);

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
                    postImages.setPostId(post.getId());
                    postImages.setSerialNum(i + 1);
                    userMapper.savePostImages(postImages);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }
        String coverUrl = null;
        if (postImageUrlList != null && postImageUrlList.size() > 0) {
            coverUrl = postImageUrlList.get(0);
        }

        return PostsVO.builder()
                .postId(post.getId())
                .userId(currentId)
                .content(post.getContent())
                .imageUrls(postImageUrlList)
                .coverUrl(coverUrl)
                .createAt(post.getCreateAt().getTime() / 1000)
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

        String currentId = BaseContext.getCurrentId();
        List<Post> postList = userMapper.getMyPosts(currentId, offset, size + 1);
        return getPostsVOPageResult(page, size, postList);
    }

    private PageResult<PostsVO> getPostsVOPageResult(int page, int size, List<Post> postList) {
        if (postList == null || postList.isEmpty()) {
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
        User user = userMapper.getById(post.getUserId());
        PostsVO.PostsVOBuilder postsVOBuilder = PostsVO.builder()
                .postId(post.getId())
                .userId(post.getUserId())
                .nickName(user.getUsername())
                .userAvatarUrl(user.getAvatarUrl())
                .content(post.getContent())
                .likeCount(post.getLikeCount())
                .createAt(post.getCreateAt().getTime() / 1000);

        List<String> imageUrlsList = Optional.ofNullable(imageMap.get(post.getId()))
                .orElse(Collections.emptyList())
                .stream()
                .sorted(Comparator.comparingInt(PostImages::getSerialNum)) // 按序号排序
                .map(PostImages::getImageUrl)
                .collect(Collectors.toList());

        String coverUrl = null;
        if (imageUrlsList != null && !imageUrlsList.isEmpty()) {
            coverUrl = imageUrlsList.get(0);
        }

        postsVOBuilder.coverUrl(coverUrl);
        postsVOBuilder.imageUrls(imageUrlsList);
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
        String username = userMapper.getById(comment.getUserId()).getUsername();
        vo.setUserName(username);
        if (comment.getReplyToUserId() != null) {
            User replyToUser = userMapper.getById(comment.getReplyToUserId());
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
        String currentId = BaseContext.getCurrentId();
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
