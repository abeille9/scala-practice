function render() {
    var route = window.location.hash.substr(1);

    var publishers = [{href: "publishers/add", text: lang.t("add"), iconClassName: "", items: ""},
        {href: "publishers/remove", text: lang.t("remove"), iconClassName: "", items: ""},
        {href: "publishers/show", text: lang.t("menu.show"), items: ""}];

    var menu = [
        {href: "publishers", text: lang.t("menu.publishers"), iconClassName: "", items: publishers},
        {href: "analysis", text: lang.t("menu.analysis"), iconClassName: "", items: ""},
        {href: "streaming", text: lang.t("menu.streaming"), iconClassName: "", items: ""}
    ];

    route = route || "analysis";
    ReactDOM.render(<MainLayout routes={menu} child={<UserApp route={route}/>}/>, document.body);

}

window.addEventListener('hashchange', render);
