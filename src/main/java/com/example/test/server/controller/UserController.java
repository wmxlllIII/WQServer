package com.example.test.server.controller;

import com.example.test.common.constant.JwtClaimsConstant;
import com.example.test.common.properties.JwtProperties;
import com.example.test.common.result.PageResult;
import com.example.test.common.result.Result;
import com.example.test.common.utils.JwtUtil;
import com.example.test.common.utils.TimeUtil;
import com.example.test.common.utils.UrlUtil;
import com.example.test.pojo.dto.*;
import com.example.test.pojo.entity.*;
import com.example.test.pojo.vo.*;
import com.example.test.server.service.UserService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/auth")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;


    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping("/sendCode")
    @ApiOperation("用户获取验证码")
    public Result<Void> sendCode(@RequestBody SendCodeDTO sendCodeDTO) {
        userService.getCode(sendCodeDTO);
        return Result.success();
    }

    @PostMapping("/register")
    @ApiOperation("用户注册")
    public Result<RegisterVO> register(@RequestBody RegisterDTO registerDTO) {
        log.info("用户注册:{}", registerDTO);
        User user = userService.register(registerDTO);

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, user.getUuNumber());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims
        );

        RegisterVO registerVO = RegisterVO.builder()
                .username(user.getUsername())
                .email(registerDTO.getEmail())
                .uuNumber(user.getUuNumber())
                .token(token)
                .avatarUrl(UrlUtil.fillUrl(user.getAvatarUrl()))
                .status("active")
                .emailVerified(false)
                .registerTime(TimeUtil.dateTimeToSecond(user.getCreateAt()))
                .build();


        return Result.success(registerVO);
    }


    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO loginDTO) {
        log.info("用户登录:#{}", loginDTO);
        User user = userService.login(loginDTO);

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, user.getUuNumber());

        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .name(user.getUsername())
                .email(loginDTO.getAuthValue())
                .uuNumber(user.getUuNumber())
                .avatarUrl(UrlUtil.fillUrl(user.getAvatarUrl()))
                .token(token)
                .build();
        log.info("userLoginVO:{}", userLoginVO);

        return Result.success(userLoginVO);
    }

    @PostMapping("/autoLogin")
    @ApiOperation("用户自动登录")
    public Result<UserLoginVO> autoLogin() {
        UserLoginVO userLoginVO = userService.autoLogin();

        return Result.success(userLoginVO);
    }

    @PostMapping(value = "/avatar")
    @ApiOperation("更新用户头像")
    public Result<UserVO> updateAvatar(@ModelAttribute AvatarUploadDTO avatarUploadDTO) {
        UserVO userVO = userService.updateAvatar(avatarUploadDTO);
//        UpdateAvatarVO updateAvatarVO = UpdateAvatarVO.builder()
//                .avatarUrl(avatarUrl)
//                .build();
//        return Result.success(UpdateAvatarVO);
        return Result.success(userVO);
    }

    @PostMapping("/updateUserinfo")
    @ApiOperation("更新用户信息")
    public Result<UserVO> updateUserInfo(@RequestBody UpdateUserinfoDTO dto) {
        UserVO userVO = userService.updateUserInfo(dto);
        return Result.success(userVO);
    }

    @PostMapping(value = "/searchUser")
    @ApiOperation(value = "搜索用户")
    public Result<SearchUserVO> getUsers(@RequestBody SearchUserDTO searchUserDTO) {
        SearchUserVO searchUserVO = userService.searchUser(searchUserDTO);
        if (searchUserVO == null) {
            return Result.error("用户不存在");
        }
        return Result.success(searchUserVO);
    }

    @PostMapping(value = "/friend/apply")
    @ApiOperation(value = "申请好友")
    public Result<FriendApplyVO> applyForFriend(@RequestBody FriendApplyDTO friendApplyDTO) {
        String state = userService.FriendApply(friendApplyDTO);
        FriendApplyVO friendApplyVO = FriendApplyVO.builder()
                .state(state)
                .build();

        return Result.success(friendApplyVO);
    }

    @PostMapping(value = "/friend/delete")
    @ApiOperation(value = "删除好友")
    public Result<Boolean> deleteFriend(@RequestBody DeleteFriendDTO dto) {
        boolean b = userService.deleteFriend(dto);
        if (b) {
            return Result.success(true);
        } else {
            return Result.error("删除失败");
        }
    }

    @PostMapping(value = "/friend/applyResult")
    @ApiOperation(value = "处理好友请求结果")
    public Result<HandleFriendRequestVO> handleFriendApplyResult(@RequestBody HandleFriendRequestDTO friendRequestDTO) {
        HandleFriendRequestVO handleFriendRequestVO = userService.handleResponse(friendRequestDTO);
        return Result.success(handleFriendRequestVO);
    }

    @PostMapping("friend/allrequests")
    @ApiOperation(value = "获取所有好友关系")
    public Result<List<Map<String, Object>>> getAllFriendRequest() {
        List<Map<String, Object>> allFriendRequest = userService.getAllFriendRequest();
        return Result.success(allFriendRequest);
    }

    @PostMapping("friend/getAllFriends")
    @ApiOperation(value = "获取所有好友")
    public Result<List<FriendInfoVO>> getAllFriend() {
        List<FriendInfoVO> friendInfoVOList = userService.getAllFriends();

        return Result.success(friendInfoVOList);
    }

    @PostMapping("msg/send")
    @ApiOperation(value = "发消息")
    public Result<List<MsgVO>> sendMsg(@RequestBody MsgDTO msgDTO) {
        return Result.success(userService.handleMsg(msgDTO));
    }

    @PostMapping("msg/getMsg")
    @ApiOperation(value = "获取消息")
    public PageResult<MsgVO> getMsg(@RequestBody GetMsgDTO getMsgDTO) {
        return userService.getMsg(getMsgDTO);
    }

    @PostMapping("movie/movies")
    @ApiOperation(value = "获取电影列表")
    public Result<List<MovieVO>> getMovies() {
        List<MovieVO> movieList = userService.getMovies();
        return Result.success(movieList);
    }

    @PostMapping("movie/rooms")
    @ApiOperation(value = "获取房间列表")
    public Result<List<RoomVO>> getRooms() {
        List<RoomVO> rooms = userService.getRooms();
        return Result.success(rooms);
    }

    @PostMapping("movie/saveRoom")
    @ApiOperation(value = "保存房间")
    public void saveRooms(@RequestBody SaveRoomDTO saveRoomDTO) {
        userService.saveRoom(saveRoomDTO);
    }

    @PostMapping("movie/removeRoom")
    @ApiOperation(value = "销毁房间")
    public void removeRoom(@RequestBody RemoveRoomDTO removeRoomDTO) {
        userService.removeRoom(removeRoomDTO);
    }

    @PostMapping("message/shareRoom")
    @ApiOperation(value = "分享消息")
    public Result<Integer> shareMessage(@RequestBody ShareMessageDTO shareDTO) {
        log.info("收到分享消息请求: {}", shareDTO);
        userService.saveShareMessage(shareDTO);
        return Result.success();
    }

//    @PostMapping("post")
//    @ApiOperation(value = "发布动态")
//    public Result<PostsVO> createPost(@RequestBody PostsDTO postsDTO) {
//        PostsVO postsVO = userService.publishPost(postsDTO);
//        return Result.success(postsVO);
//    }

    @PostMapping("/post")
    @ApiOperation(value = "发布动态")
    public Result<PostsVO> createPost(@ModelAttribute PostDTO postsDTO) {
        PostsVO postsVO = userService.publishPost(postsDTO);
        return Result.success(postsVO);
    }

    @PostMapping("/getPost")
    @ApiOperation("分页获取动态")
    public PageResult<PostsVO> getPost(@RequestBody PostsQueryDTO postsQueryDTO) {
        return userService.getPosts(postsQueryDTO);
    }

    @PostMapping("/getMyPost")
    @ApiOperation("分页获取自己作品")
    public PageResult<PostsVO> getMyPost(@RequestBody PostsQueryDTO postsQueryDTO) {
        return userService.getMyPosts(postsQueryDTO);
    }

    @PostMapping("/getFollowerPost")
    @ApiOperation("分页获取关注用户作品")
    public PageResult<PostsVO> getFollowerPost(@RequestBody PostsQueryDTO postsQueryDTO) {
        return userService.getFollowerPost(postsQueryDTO);
    }

    @PostMapping("getComment")
    @ApiOperation("分页获取评论")
    public PageResult<GetCommentVO> getComment(@RequestBody GetCommentDTO commentDTO) {
        return userService.getComment(commentDTO);
    }

    @PostMapping("addComment")
    @ApiOperation("发布评论")
    public Result<Comment> addComment(@RequestBody AddCommentDTO addCommentDTO) {
        return userService.addComment(addCommentDTO);
    }

    @PostMapping("/getStsPermission")
    @ApiOperation("获取上传权限")
    public Result<StsVO> getSts() {
        StsVO sts = userService.getSts();
        return Result.success(sts);
    }

    @PostMapping("/followUser")
    @ApiOperation("关注用户")
    public Result<FollowUserVO> followUser(@RequestBody FollowUserDTO dto) {
        FollowUserVO followUserVO = userService.followUser(dto);
        return Result.success(followUserVO);
    }

    @PostMapping("/unFollowUser")
    @ApiOperation("取关用户")
    public Result<UnFollowUserVO> unFollowUser(@RequestBody FollowUserDTO dto) {
        UnFollowUserVO unFollowUserVO = userService.unFollowUser(dto);
        return Result.success(unFollowUserVO);
    }

    @PostMapping("/getMovieCategory")
    @ApiOperation("获取电影分类")
    public Result<List<MovieCateVO>> getMovieCategory() {
        List<MovieCateVO> unFollowUserVO = userService.getMovieCategory();
        return Result.success(unFollowUserVO);
    }

    @PostMapping("/saveMovieProgress")
    @ApiOperation("保存电影进度")
    public Result<Boolean> saveMovieProgress(@RequestBody SaveProgressDTO dto) {
        userService.saveMovieProgress(dto);
        return Result.success();
    }

    @PostMapping("/getWatchHistory")
    @ApiOperation("获取观看电影历史")
    public Result<List<MovieHistoryVO>> getWatchHistory() {
        List<MovieHistoryVO> watchHistory = userService.getWatchHistory();
        return Result.success(watchHistory);
    }

    @PostMapping("/getActorProfile")
    @ApiOperation("获取演员个人信息")
    public Result<ActorProfileVO> getActorProfile(@RequestBody ActorProfileDTO dto) {
        ActorProfileVO actorProfileVO = userService.getActorProfile(dto);
        if (actorProfileVO == null) {
            log.info("[x] getActorProfile #312");
            return Result.error("演员不存在");
        }
        return Result.success(actorProfileVO);
    }

    @PostMapping("/likePost")
    @ApiOperation("喜欢帖子")
    public Result<Boolean> likePost(@RequestBody LikePostDTO dto) {
        Boolean isSuccess = userService.likePostIfNeed(dto);
        return Result.success(isSuccess);
    }

    @PostMapping("/getLikePost")
    @ApiOperation("获取喜欢帖子")
    public Result<List<PostsVO>> getLikePost() {
        List<PostsVO> postsVOList = userService.getLikePost();
        return Result.success(postsVOList);
    }

    @PostMapping("/getFootprintPost")
    @ApiOperation("获取足迹帖子")
    public Result<List<PostsVO>> getFootprintPost() {
        List<PostsVO> postsVOList = userService.getFootprintPost();
        return Result.success(postsVOList);
    }

}
