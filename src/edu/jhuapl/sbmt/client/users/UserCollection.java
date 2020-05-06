package edu.jhuapl.sbmt.client.users;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class UserCollection
{
    public static UserCollection of(Iterable<User> users)
    {
        Preconditions.checkNotNull(users);

        LinkedHashMap<String, User> usersMap = new LinkedHashMap<>();
        LinkedHashSet<String> groupIds = new LinkedHashSet<>();

        for (User user : users)
        {
            usersMap.put(user.getId(), user);
            groupIds.addAll(user.getGroupIds());
        }

        return new UserCollection(usersMap, groupIds);
    }

    public static UserCollection of(Iterable<User> users, Iterable<String> groupIds)
    {
        Preconditions.checkNotNull(users);
        Preconditions.checkNotNull(groupIds);

        LinkedHashMap<String, User> usersMap = new LinkedHashMap<>();
        for (User user : users)
        {
            usersMap.put(user.getId(), user);
        }

        LinkedHashSet<String> groupIdsList = new LinkedHashSet<>();
        for (String groupId : groupIds)
        {
            groupIdsList.add(groupId);
        }

        return new UserCollection(usersMap, groupIdsList);
    }

    private final LinkedHashMap<String, User> users;
    private final LinkedHashSet<String> groupIds;

    protected UserCollection(LinkedHashMap<String, User> users, LinkedHashSet<String> groupIds)
    {
        this.users = users;
        this.groupIds = groupIds;
    }

    public User getUser(String id)
    {
        User result = users.get(id);

        if (result == null)
        {
            result = User.ofUnauthorized();
        }

        return result;
    }

    public ImmutableList<User> getUsers()
    {
        return ImmutableList.copyOf(users.values());
    }

    public ImmutableList<String> getGroupIds()
    {
        return ImmutableList.copyOf(groupIds);
    }

}
